package com.tamelea.pm.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.tamelea.pm.PeriMeleon;

public final class Data {
	public static final String					SAVE_NEEDED			= "saveNeeded";
	public static final String					BOUND_FILE			= "boundFile";
	public static final String					MEMBER_ADDED		= "memberAdded";
	public static final String					MEMBER_REMOVED		= "memberRemoved";
	public static final String					MEMBERS_CHANGED		= "membersChanged";
	public static final String					HOUSEHOLD_ADDED		= "householdAdded";
	public static final String					HOUSEHOLD_REMOVED	= "householdRemoved";
	public static final String					HOUSEHOLD_CHANGED	= "householdChanged";
	private static final SAXReader				reader				= new SAXReader(false);
	private static final Pattern				datePattern			= Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d?\\d?");
	private PropertyChangeSupport				propertyHandler;
	private boolean								saveNeeded;
	private File								boundFile;

	private HashMap<MemberIndex, Member>		members;
	private int									nextMemberIndex;

	private HashMap<HouseholdIndex, Household>	households;
	private int									nextHouseholdIndex;
	private HashMap<AddressIndex, Address>		addresses;
	private int									nextAddressIndex;
	
	public Data() {
		this.boundFile = null;
	    propertyHandler = new PropertyChangeSupport(this);
		constructStructures();
	}
	
	private void constructStructures() {
		members = new HashMap<MemberIndex, Member>();
		nextMemberIndex = 0;
		households = new HashMap<HouseholdIndex, Household>();
		nextHouseholdIndex = 0;
		addresses = new HashMap<AddressIndex, Address>();
		nextAddressIndex = 0;
		saveNeeded = false;
	}
	
	public void removeAll() {
		constructStructures();
		setSaveNeeded(false);
		setBoundFile(null);
		firePropertyChange(MEMBERS_CHANGED);
	}
	
	public MemberIndex addMember() {
		MemberIndex index = new MemberIndex(nextMemberIndex++);
		Member member = new Member(index);
		members.put(index, member);
//		System.out.println("Added index " + index + " ct: " + members.size());
		setSaveNeeded(true);
		firePropertyChange(MEMBER_ADDED);
		return index;
	}
	
	/**
	 * Set one field at a time.
	 * DON'T FIRE PROPERTY CHANGE!
	 * @param index
	 * @param field
	 * @param value
	 */
	public void setMemberValue(MemberIndex index, MemberField field, Object value) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		if (field == MemberField.HOUSEHOLD) {
			HouseholdIndex oldHousehold = (HouseholdIndex)getMemberValue(index, MemberField.HOUSEHOLD);
			changeHouseholdsForMember(index, oldHousehold, (HouseholdIndex)value);
		}
		member.setValue(field, value);
		setSaveNeeded(true);
	}
	
	/**
	 * Set a group of fields at a time, causing only one property change.
	 * @param index
	 * @param values
	 */
	public void setMemberValues(MemberIndex index, Map<MemberField, Object> values) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		for (MemberField field : values.keySet()) {
			if (field == MemberField.HOUSEHOLD) {
				HouseholdIndex oldHousehold = (HouseholdIndex)getMemberValue(index, MemberField.HOUSEHOLD);
				changeHouseholdsForMember(index, oldHousehold, (HouseholdIndex)values.get(field));
			}
			member.setValue(field, values.get(field));
		}
		setSaveNeeded(true);
		firePropertyChange(MEMBERS_CHANGED);
	}
	
	/**
	 * Called only when Member data is edited by user.
	 * Don't call it elsewhere!
	 * @param index
	 */
	public void updateLastChange(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		member.setValue(MemberField.LAST_CHANGE, PMDate.getToday());
		setSaveNeeded(true);
	}
	
	/**
	 * Makes necessary adjustments to household when member household changes.
	 * It DOESN'T change the member field (thus avoiding an infinite recursion).
	 * If the member is added to a new household, they go into "others".
	 * @param member
	 * @param oldHousehold
	 * @param newHousehold
	 */
	private void changeHouseholdsForMember(
			MemberIndex member, 
			HouseholdIndex oldHousehold, 
			HouseholdIndex newHousehold) 
	{
		if (oldHousehold != null) {
			if (newHousehold != null) {
				if (!oldHousehold.equals(newHousehold)) {
					removeMemberFromHousehold(member, oldHousehold);
					setOtherMembershipWithoutFixup(newHousehold, member, true);
					firePropertyChange(HOUSEHOLD_CHANGED);
				}
			} else {
				removeMemberFromHousehold(member, oldHousehold);
				firePropertyChange(HOUSEHOLD_CHANGED);
			}
		} else if (newHousehold != null) {
			setOtherMembershipWithoutFixup(newHousehold, member, true);
			firePropertyChange(HOUSEHOLD_CHANGED);
		}
	}
	
	private void removeMemberFromHousehold(MemberIndex member, HouseholdIndex household) {
		MemberIndex head = (MemberIndex)getHouseholdValue(household, HouseholdField.HEAD);
		MemberIndex spouse = (MemberIndex)getHouseholdValue(household, HouseholdField.SPOUSE);
		if (member.equals(head)) {
			setHouseholdValueWithoutFixup(household, HouseholdField.HEAD, null);
		} else if (member.equals(spouse)) {
			setHouseholdValueWithoutFixup(household, HouseholdField.SPOUSE, null);
			return;
		} else {
			setOtherMembershipWithoutFixup(household, member, false);
		}
	}
	
	public int getOthersSize(HouseholdIndex index) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No Household for " + index);
		return household.getOthers().size();
	}
	
	public Object getMemberValue(MemberIndex index, MemberField field) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		return member.getValue(field);
	}
	
	public boolean isActive(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		return member.isActive();
	}
	
	public Transaction addTransaction(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		return member.addTransaction();
	}
	
	public void removeTransaction(MemberIndex index, Transaction transaction) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		member.remove(transaction);
		setSaveNeeded(true);
		firePropertyChange(MEMBERS_CHANGED);
	}
	
	public void setTransactionValue(Transaction transaction, TransactionField field, Object value) {
		transaction.setValue(field, value);
		setSaveNeeded(true);
	}
	
	public void setTransactionValues(Transaction transaction, Map<TransactionField, Object> values) {
		for (TransactionField field : values.keySet()) {
			transaction.setValue(field, values.get(field));
		}
		setSaveNeeded(true);
		firePropertyChange(MEMBERS_CHANGED);
	}

	public List<Transaction> getSortedTransactions(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		List<Transaction> list = member.getTransactions();
		Collections.sort(list);
		return list;
	}
	
	public Transaction getMostRecentTransaction(MemberIndex index) {
		List<Transaction> list = getSortedTransactions(index);
		if (list.isEmpty()) return null;
		return list.get(list.size() - 1);
	}
	
	public Service addService(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		return member.addService();
	}
	
	public void removeService(MemberIndex index, Service service) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		member.remove(service);
		setSaveNeeded(true);
		firePropertyChange(MEMBERS_CHANGED);
	}
	
	public void setServiceValues(Service service, Map<ServiceField, Object> values) {
		for (ServiceField field : values.keySet()) {
			service.setValue(field, values.get(field));
		}
		setSaveNeeded(true);
		firePropertyChange(MEMBERS_CHANGED);
	}

	public List<Service> getSortedServices(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		List<Service> list = member.getServices();
		Collections.sort(list);
		return list;
	}
	
	public void removeMember(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		members.remove(index);
		firePropertyChange(MEMBER_REMOVED);
		HouseholdIndex householdIndex = (HouseholdIndex)member.getValue(MemberField.HOUSEHOLD);
		if (householdIndex != null) {
			Household household = households.get(householdIndex);
			if (household == null) throw new IllegalArgumentException("No Household for " + householdIndex);
			household.removeMember(index);
			firePropertyChange(HOUSEHOLD_CHANGED);
		}
		setSaveNeeded(true);
	}
	
	public HouseholdIndex addHousehold() {
		HouseholdIndex index = new HouseholdIndex(nextHouseholdIndex++);
		Household household = new Household(index);
		households.put(index, household);
		//This is only called in contexts where data will be filled in, triggering all this stuff
		//If property change is fired before data are filled in it's a NPE
//		setSaveNeeded(true);
//		householdSortNeeded = true;
//		firePropertyChange(HOUSEHOLD_ADDED);
		return index;
	}
	
	public AddressIndex addAddress() {
		AddressIndex index = new AddressIndex(nextAddressIndex++);
		Address address = new Address(index);
		addresses.put(index, address);
		//Don't fire too soon; rely on data changes to follow
//		setSaveNeeded(true);
//		householdSortNeeded = true;
//		firePropertyChange(HOUSEHOLD_ADDED);
		return index;
	}
	
	/**
	 * Set one field.
	 * DON'T FIRE PROPERTY CHANGE!
	 * @param index
	 * @param field
	 * @param value
	 */
	public void setHouseholdValue(HouseholdIndex index, HouseholdField field, Object value) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No Household for " + index);
		if (field == HouseholdField.HEAD)
			fixupHead(index, household, (MemberIndex)value);
		if (field == HouseholdField.SPOUSE)
			fixupSpouse(index, household, (MemberIndex)value);
		household.setValue(field, value);
		setSaveNeeded(true);
	}
	
	/**
	 * Same as setHouseholdValue(), but no fixup of member.
	 * @param index
	 * @param field
	 * @param value
	 */	
	public void setHouseholdValueWithoutFixup(HouseholdIndex index, HouseholdField field, Object value) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No Household for " + index);
		household.setValue(field, value);
		setSaveNeeded(true);
	}
	
	/**
	 * Set group of fields, causing only one property change.
	 * @param index
	 * @param values
	 */
	public void setHouseholdValues(HouseholdIndex index, Map<HouseholdField, Object> values) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No Household for " + index);
		for (HouseholdField field : values.keySet()) {
			//These referential integrity rules merely interfere with setting values.
			//To do this right, I'd have to define a "change head" transaction.
			//For now, we place the burden on the keeper of the DB.
//			if (field == HouseholdField.HEAD)
//				fixupHead(index, household, (MemberIndex)values.get(field));
//			if (field == HouseholdField.SPOUSE)
//				fixupSpouse(index, household, (MemberIndex)values.get(field));
			household.setValue(field, values.get(field));
		}
		setSaveNeeded(true);
		firePropertyChange(HOUSEHOLD_CHANGED);
	}
	
	/**
	 * Keep the household fields consistent with new and old heads.
	 * This seems like huge amounts of work!
	 * @param index
	 * @param household
	 * @param newHead
	 */
	private void fixupHead(HouseholdIndex index, Household household, MemberIndex newHead) {
		MemberIndex oldHead = (MemberIndex)household.getValue(HouseholdField.HEAD);
		if (oldHead != null) {
			if (newHead != null) {
				if (!newHead.equals(oldHead)) {
					members.get(oldHead).setValue(MemberField.HOUSEHOLD, null);
					members.get(newHead).setValue(MemberField.HOUSEHOLD, index);
				}
			} else {
				members.get(oldHead).setValue(MemberField.HOUSEHOLD, null);
			}
		} else {
			if (newHead != null) {
				members.get(newHead).setValue(MemberField.HOUSEHOLD, index);
			}
		}
	}
	
	private void fixupSpouse(HouseholdIndex index, Household household, MemberIndex newSpouse) {
		MemberIndex oldSpouse = (MemberIndex)household.getValue(HouseholdField.SPOUSE);
		if (oldSpouse != null) {
			if (newSpouse != null) {
				if (!newSpouse.equals(oldSpouse)) {
					members.get(oldSpouse).setValue(MemberField.HOUSEHOLD, null);
					members.get(newSpouse).setValue(MemberField.HOUSEHOLD, index);
				}
			} else {
				members.get(oldSpouse).setValue(MemberField.HOUSEHOLD, null);
			}
		} else {
			if (newSpouse != null) {
				members.get(newSpouse).setValue(MemberField.HOUSEHOLD, index);
			}
		}
	}
	
	public Object getHouseholdValue(HouseholdIndex index, HouseholdField field) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No Household for " + index);
		return household.getValue(field);
	}
	
	/**
	 * Same as getHouseholdValue, but returns blank string if index is null.
	 * @param index
	 * @param field
	 * @return
	 */
	public Object getHouseholdValueLenient(HouseholdIndex index, HouseholdField field) {
		if (index == null) return "";
		Household household = households.get(index);
		if (household == null) return "";
		return household.getValue(field);
	}
	
	public Object getHouseholdValue(HouseholdIndex index, AddressField field) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No Household for " + index);
		AddressIndex addressIndex = (AddressIndex)household.getValue(HouseholdField.ADDRESS);
		Address address = addresses.get(addressIndex);
		if (address == null) throw new IllegalArgumentException("No Address for " + index);
		return address.getValue(field);
	}
	
	/**
	 * Same as getHouseholdValue, but returns blank string if index is null.
	 * @param index
	 * @param field
	 * @return
	 */
	public Object getHouseholdValueLenient(HouseholdIndex index, AddressField field) {
		if (index == null) return "";
		Household household = households.get(index);
		if (household == null) return "";
		AddressIndex addressIndex = (AddressIndex)household.getValue(HouseholdField.ADDRESS);
		Address address = addresses.get(addressIndex);
		if (address == null) return "";
		return address.getValue(field);
	}
	
	public List<MemberIndex> getInhabitantsByAge(HouseholdIndex household) {
		ArrayList<MemberIndex> result = new ArrayList<MemberIndex>();
		MemberIndex head = (MemberIndex)getHouseholdValue(household, HouseholdField.HEAD);
		if (head != null) result.add(head);
		MemberIndex spouse = (MemberIndex)getHouseholdValue(household, HouseholdField.SPOUSE);
		if (spouse != null) result.add(spouse);
		result.addAll(households.get(household).getOthers());
		Collections.sort(result, new AgeComparator(this));
		return result;
	}
	
	public List<MemberIndex> getChildrenByAge(HouseholdIndex household) {
		ArrayList<MemberIndex> result = new ArrayList<MemberIndex>();
		result.addAll(households.get(household).getOthers());
		Collections.sort(result, new AgeComparator(this));
		return result;
	}
	
	/**
	 * Set one field.
	 * DON'T FIRE PROPERTY CHANGE!
	 * @param index
	 * @param field
	 * @param value
	 */
	public void setAddressValue(AddressIndex index, AddressField field, Object value) {
		Address address = addresses.get(index);
		if (address == null) throw new IllegalArgumentException("No Address for " + index);
		address.setValue(field, value);
	}
	
	public Object getAddressValue(AddressIndex index, AddressField field) {
		Address address = addresses.get(index);
		if (address == null) throw new IllegalArgumentException("No Address for " + index);
		return address.getValue(field);
	}
	
	public String getContactInfo(MemberIndex member) {
		StringBuffer sb = new StringBuffer();
		String lastName = ((PMString)(getMemberValue(member, MemberField.LAST_NAME))).toString();
		sb.append(lastName + ", " + getInformalFrstName(member) + "\n");
		HouseholdIndex household = (HouseholdIndex)getMemberValue(member, MemberField.HOUSEHOLD);
		AddressIndex addressIndex = (AddressIndex)getMemberValue(member, MemberField.TEMP_ADDRESS);
		if (addressIndex == null)
			addressIndex = (AddressIndex)getHouseholdValue(household, HouseholdField.ADDRESS);
		if (addressIndex != null) {
			String address = ((PMString)getAddressValue(addressIndex, AddressField.ADDRESS)).toString();
			sb.append(address + "\n");
			String address2 = ((PMString)getAddressValue(addressIndex, AddressField.ADDRESS_2)).toString();
			if (address2.length() > 0)
				sb.append(address2 + "\n");
			String city = ((PMString)getAddressValue(addressIndex, AddressField.CITY)).toString();
			String state = ((PMString)getAddressValue(addressIndex, AddressField.STATE)).toString();
			String postal = ((PMString)getAddressValue(addressIndex, AddressField.POSTAL_CODE)).toString();
			sb.append(city + ", " + state + " " + postal + "\n");
			String country = ((PMString)getAddressValue(addressIndex, AddressField.COUNTRY)).toString();
			if (country.length() > 0)
				sb.append(country + "\n");
			String phone = ((Phone)getAddressValue(addressIndex, AddressField.HOME_PHONE)).toString();
			if (phone.length() > 0) sb.append(phone + "\n");
		}
		ArrayList<String> entries = new ArrayList<String>();
		String eMail = ((PMString)getMemberValue(member, MemberField.EMAIL)).toString();
		if (eMail.length() > 0) entries.add(eMail);
		String workEMail = ((PMString)getMemberValue(member, MemberField.WORK_EMAIL)).toString();
		if (workEMail.length() > 0) entries.add(workEMail + "(w)");
		String mobilePhone = ((Phone)getMemberValue(member, MemberField.MOBILE_PHONE)).toString();
		if (mobilePhone.length() > 0) entries.add(mobilePhone + "(m)");
		String workPhone = ((Phone)getMemberValue(member, MemberField.WORK_PHONE)).toString();
		if (workPhone.length() > 0) entries.add(workPhone + "(w)");
		if (!entries.isEmpty()) {
			sb.append(entries.get(0));
			for (int i = 1; i < entries.size(); ++i) {
				sb.append(" | " + entries.get(i));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void readAgain(InputStream inputStream)
	throws DocumentException, PMParseException
	{
	    Document document = reader.read(inputStream);
	    parseDocument(document);
		firePropertyChange(MEMBERS_CHANGED);
		//reset save needed after the property changes have propagated
		setSaveNeeded(false);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyHandler.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyHandler.removePropertyChangeListener(listener);
	}
	
	public void firePropertyChange(String propertyName) {
		propertyHandler.firePropertyChange(propertyName, null, null);
	}
	
	public void firePropertyChange(String propertyName,
			String oldValue,
			String newValue)
	{
		propertyHandler.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	public void firePropertyChange(String propertyName,
			boolean oldValue,
			boolean newValue)
	{
		propertyHandler.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	public boolean isSaveNeeded() {
		return saveNeeded;
	}
	
	public void setSaveNeeded(boolean value) {
		boolean oldValue = saveNeeded;
		saveNeeded = value;
		firePropertyChange(SAVE_NEEDED, oldValue, saveNeeded);
	}
	
	public File getBoundFile() {
		return boundFile;
	}
	
	public void setBoundFile(File file) {
		File oldValue = boundFile;
		boundFile = file;
		firePropertyChange(
				BOUND_FILE, 
				(oldValue == null) ? "null" : oldValue.getName(),
				(boundFile == null) ? "null" : boundFile.getName());
	}
	
	public String getMemberDisplayName(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) return null;
		return makeDisplayName(member);
	}
	
	public String getInformalFrstName(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) return null;
		String nickName = ((PMString)member.getValue(MemberField.NICK_NAME)).toString();
		if (nickName != null && nickName.length() > 0) return nickName;
		return ((PMString)member.getValue(MemberField.FIRST_NAME)).toString();
	}
	
	/**
	 * last name, nick name.
	 * @param index
	 * @return
	 */
	public String makeInformalName(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) return null;
		String informalFirst = getInformalFrstName(index);
		String lastName = ((PMString)member.getValue(MemberField.LAST_NAME)).toString();
		return lastName + ", " + informalFirst;
	}
	
	String makeDisplayName(MemberIndex index) {
		Member member = members.get(index);
		if (member == null) throw new IllegalArgumentException("No Member for " + index);
		return makeDisplayName(member);

	}
	
	private String makeDisplayName(Member member) {
		String displayName = member.getValue(MemberField.LAST_NAME) 
			+ ", " + member.getValue(MemberField.FIRST_NAME);
		PMString middle = (PMString)member.getValue(MemberField.MIDDLE_NAME);
		if (middle != null && middle.length() > 0) displayName += " " + middle;
		PMString maiden = (PMString)member.getValue(MemberField.MAIDEN_NAME);
		if (maiden != null && maiden.length() > 0) displayName += " (" + maiden + ")";
		PMString suffix = (PMString)member.getValue(MemberField.SUFFIX);
		if (suffix != null && suffix.length() > 0) displayName += ", " + suffix;
		PMString nickString = (PMString) (member.getValue(MemberField.NICK_NAME));
		if (nickString != null && nickString.length() > 0) {
			displayName = displayName + " \"" + nickString + "\"";
		}
		return displayName;
	}
	
	public List<MemberIndex> getDirectoryMembers() {
		HashSet<MemberIndex> resultSet = new HashSet<MemberIndex>();
		//all active heads of household
		for (HouseholdIndex householdIndex : households.keySet()) {
			MemberIndex head = (MemberIndex)getHouseholdValue(householdIndex, HouseholdField.HEAD);
			//"Deceased" household has no head
			if (head != null && isActive(head)) resultSet.add(head);
		}
		//and all active members with temporary address, but no duplicates
		for (MemberIndex memberIndex : members.keySet()) {
			if (!isActive(memberIndex)) continue;
			boolean hasTempAddress = getMemberValue(memberIndex, MemberField.TEMP_ADDRESS) != null;
			if (hasTempAddress) resultSet.add(memberIndex);
		}
		ArrayList<MemberIndex> result = new ArrayList<MemberIndex>(resultSet);
		Collections.sort(result, new InformalNameComparator(this));
		return result;
	}
	
	public List<String> getNamesInHousehold(HouseholdIndex index) {
		ArrayList<String> result = new ArrayList<String>();
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No household for " + index);
		MemberIndex head = (MemberIndex)household.getValue(HouseholdField.HEAD);
		if (head != null) result.add(makeDisplayName(members.get(head)));
		MemberIndex spouse = (MemberIndex)household.getValue(HouseholdField.SPOUSE);
		if (spouse != null) result.add(makeDisplayName(members.get(spouse)));
		for (MemberIndex otherIndex : household.getOthers()) {
			result.add(makeDisplayName(members.get(otherIndex)));
		}
		return result;
	}
	
	public void removeHousehold(HouseholdIndex index) {
		Household household = households.get(index);
		if (household == null) throw new IllegalArgumentException("No household for " + index);
		MemberIndex head = (MemberIndex)household.getValue(HouseholdField.HEAD);
		if (head != null) members.get(head).setValue(MemberField.HOUSEHOLD, null);
		MemberIndex spouse = (MemberIndex)household.getValue(HouseholdField.SPOUSE);
		if (spouse != null) members.get(spouse).setValue(MemberField.HOUSEHOLD, null);
		for (MemberIndex otherIndex : household.getOthers()) {
			members.get(otherIndex).setValue(MemberField.HOUSEHOLD, null);
		}
		firePropertyChange(MEMBERS_CHANGED);
		AddressIndex addressIndex = (AddressIndex)getHouseholdValue(
				index, HouseholdField.ADDRESS);
		households.remove(index);
		addresses.remove(addressIndex);
		firePropertyChange(HOUSEHOLD_REMOVED);
		setSaveNeeded(true);
	}
	
	/**
	 * Sorted Map of display names of members in this household, or not in any household.
	 * @param index
	 * @return map
	 */
	public List<MemberIndex> getHouseholdCandidates(HouseholdIndex index) {
		Household household = households.get(index);
		ArrayList<MemberIndex> result = new ArrayList<MemberIndex>();
		for (MemberIndex member : members.keySet()) {
			if (household.containsAsOther(member) || 
					getMemberValue(member, MemberField.HOUSEHOLD) == null)
				result.add(member);
		}
		Collections.sort(result, new AgeComparator(this));
		return result;
	}
	
	boolean containsAsOther(HouseholdIndex householdIndex, MemberIndex member) {
		Household household = households.get(householdIndex);
		if (household == null) throw new IllegalArgumentException("No household for " + householdIndex);
		return household.containsAsOther(member);
	}
	
	/**
	 * Change membership in 'other' set.
	 * Fix up corresponding Member HOUSEHOLD field.
	 * Should be used only by member editor!
	 * @param householdIndex
	 * @param memberIndex
	 * @param belongs
	 */
	public void setOtherMembership(HouseholdIndex householdIndex, MemberIndex memberIndex, boolean belongs) {
		Household household = households.get(householdIndex);
		if (household == null) throw new IllegalArgumentException("No household for " + householdIndex);
		household.setOtherMembership(memberIndex, belongs);
		Member member = members.get(memberIndex);
		if (member == null) throw new IllegalArgumentException("No member for " + memberIndex);
		//These rules just interfere with changing the DB. Leave integrity up to the DB maintainer.
//		if (belongs) member.setValue(MemberField.HOUSEHOLD, householdIndex);
//		else member.setValue(MemberField.HOUSEHOLD, null);
		firePropertyChange(MEMBERS_CHANGED);
		firePropertyChange(HOUSEHOLD_CHANGED);
		setSaveNeeded(true);
	}
	
	/**
	 * Same as setOtherMembership(), but without changes to member's HOUSEHOLD field.
	 * @param householdIndex
	 * @param memberIndex
	 * @param belongs
	 */
	public void setOtherMembershipWithoutFixup(HouseholdIndex householdIndex, MemberIndex memberIndex, boolean belongs) {
		Household household = households.get(householdIndex);
		if (household == null) throw new IllegalArgumentException("No household for " + householdIndex);
		household.setOtherMembership(memberIndex, belongs);
		firePropertyChange(HOUSEHOLD_CHANGED);
		setSaveNeeded(true);
	}

	/**
	 * Earliest DOB for given age.
	 * @param minimumAge
	 * @return earliest DOB
	 */
	static Date getEarliestDOB(int minimumAge) {
		GregorianCalendar today = new GregorianCalendar();
		today.add(Calendar.YEAR, -minimumAge);
		Date earliestDOB = new Date(today.getTimeInMillis());
		return earliestDOB;
	}
	
	/**
	 * Return filtered and sorted list of member indexes.
	 * If filter is null, all indexes are returned.
	 * If comparator is null, list is not sorted.
	 */
	public List<MemberIndex> queryMembers(MemberFilter filter, Comparator<MemberIndex> comparator) {
		List<MemberIndex> filteredMembers = new ArrayList<MemberIndex>();
		for (MemberIndex index: members.keySet()){
			if (filter != null) {
				if (filter.match(index)) filteredMembers.add(index);
    		} else filteredMembers.add(index);
		}
		if (comparator != null) Collections.sort(filteredMembers, comparator);
		return filteredMembers;
	}
	
	/**
	 * Return filtered and sorted list of household indexes.
	 * If filter is null, all indexes are returned.
	 * If comparator is null, list is not sorted.
	 */
	public List<HouseholdIndex> queryHouseholds(HouseholdFilter filter, Comparator<HouseholdIndex> comparator) {
		List<HouseholdIndex> filteredHouseholds = new ArrayList<HouseholdIndex>();
		for (HouseholdIndex index: households.keySet()){
			if (filter != null) {
				if (filter.match(index)) filteredHouseholds.add(index);
    		} else filteredHouseholds.add(index);
		}
		if (comparator != null) Collections.sort(filteredHouseholds, comparator);
		return filteredHouseholds;
	}
	
	/**
	 * Return filtered and sorted list of Transactions from all Members.
	 * @param filter
	 * @param comparator
	 * @return
	 */
	public List<Transaction> queryTransactions(
			TransactionFilter filter, 
			Comparator<Transaction> comparator) 
	{
		List<Transaction> result = new ArrayList<Transaction>();
		for (Member member : members.values()) {
			List<Transaction> transactions = member.getTransactions();
			if (filter == null)
				result.addAll(transactions);
			else {
				for (Transaction transaction : transactions)
					if (filter.match(transaction))
						result.add(transaction);
			}
		}
		if (comparator != null) Collections.sort(result, comparator);
		return result;
	}
	
	public List<BaptismQueryRecord> queryBaptisms(PMDate startDate, PMDate endDate) {
		List<BaptismQueryRecord> result = new ArrayList<BaptismQueryRecord>();		
		for (Member member : members.values()) {
			String baptismString = member.getValue(MemberField.BAPTISM).toString();
			Matcher matcher = datePattern.matcher(baptismString);
			if (matcher.find()) {
				try {
					String dateString = baptismString.substring(matcher.start(), matcher.end());
					PMDate date = new PMDate(dateString);
					if (startDate.compareTo(date) <= 0 && endDate.compareTo(date) >= 0) {
						BaptismQueryRecord bqr = new BaptismQueryRecord(date, makeDisplayName(member));
						result.add(bqr);
					}
				} catch (ParseException noted) { }
			}
		}
		Collections.sort(result); //see comparator on BaptismQueryRecord
		return result;
	}
	
	/**
	 * Return list of display names corresponding to index list.
	 * @param indexes
	 * @return name list
	 */
	public List<String> listDisplayNames(List<MemberIndex> indexes) {
		ArrayList<String> result = new ArrayList<String>();
		for (MemberIndex index : indexes) result.add(makeDisplayName(index));
		return result;
	}
	
	public List<String> listHouseholdNames(List<HouseholdIndex> indexes) {
		ArrayList<String> result = new ArrayList<String>();
		for (HouseholdIndex index : indexes) {
			Household household = households.get(index);
			result.add(((PMString)household.getValue(HouseholdField.NAME)).toString());
		}
		return result;
	}
	
	private void parseDocument(Document document) 
	throws PMParseException
	{
		constructStructures();
		Element root = document.getRootElement();
		if (!root.getName().equals("members")) throw new PMParseException(
				"Not a members file (root element is \"" + root.getName() + "\")");
		List<?> memberElements = root.elements("member");
		for (Iterator<?> i = memberElements.iterator(); i.hasNext(); ) {
			Element memberElement = (Element)i.next();
			Member member = new Member(memberElement);
			members.put(member.getIndex(), member);
			if (member.getIndex().value >= nextMemberIndex) 
				nextMemberIndex = member.getIndex().value + 1;
		}
		List<?> householdElements = root.elements("household");
		for (Iterator<?> i = householdElements.iterator(); i.hasNext(); ) {
			Element householdElement = (Element)i.next();
			Household household = new Household(householdElement);
			households.put(household.getIndex(), household);
			if (household.getIndex().value >= nextHouseholdIndex) 
				nextHouseholdIndex = household.getIndex().value + 1;
		}
		List<?> addressElements = root.elements("address");
		for (Iterator<?> i = addressElements.iterator(); i.hasNext(); ) {
			Element addressElement = (Element)i.next();
			Address address = new Address(addressElement);
			addresses.put(address.getIndex(), address);
			if (address.getIndex().value >= nextAddressIndex) 
				nextAddressIndex = address.getIndex().value + 1;
		}
	}
	
	/**
	 * This uses about the least efficient method of saving imaginable,
	 * i.e., creating a DOM in memory and pretty-printing it.
	 * But it's easy to program and is probably fast enough
	 */
	public void save(OutputStream os) 
	throws java.io.IOException
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(os, format);
		writer.write(serialize());
		writer.flush();
		setSaveNeeded(false);
	}
	
	public Document serialize() {
		Document document = DocumentHelper.createDocument();
		document.addComment("Data from " + PeriMeleon.version + " written " + new Date());
		Element root = document.addElement("members");
		for (Member member : members.values()) member.save(root);
		for (Household household : households.values()) household.save(root);
		for (Address address : addresses.values()) address.save(root);
		return document;
	}
	
	@SuppressWarnings("unchecked")
	public void exportJSON(OutputStream os) 
	throws java.io.IOException
	{
		JSONObject obj = new JSONObject();
		JSONArray marray = new JSONArray();
		for (Member member: members.values()) marray.add(member.makeJSON());
		obj.put("members",  marray);
		
		JSONArray harray = new JSONArray();
		for (Household household : households.values()) harray.add(household.makeJSON());
		obj.put("households",  harray);
		
		JSONArray aarray = new JSONArray();
		for (Address address : addresses.values()) aarray.add(address.makeJSON());
		obj.put("addresses",  aarray);
		
		PrintStream ps = new PrintStream(os);
		ps.println(obj.toString(2));
		ps.close();
	}
	
	/**
	 * All field classes must have a static method valueOf that converts a String to a new
	 * instance of the class.
	 * For those classes for which null is a legit value, e.g., PMDate, MemberIndex, 
	 * HouseholdIndex, an empty string should return a null.
	 * @param fieldClass Class of the field
	 * @param string to be parsed, NOT null
	 * @return new field object, perhaps null
	 * @throws ValueOfException 
	 */
	static Object fieldValueOf(Class<?> fieldClass, String string)
	throws ValueOfException 
	{
		if (string == null) throw new IllegalArgumentException("null string not allowed");
		try {
			Method valueOfMethod = fieldClass.getDeclaredMethod("valueOf", String.class);
			return valueOfMethod.invoke(null, string);
		} catch (SecurityException e) {
			throw new ValueOfException(e);
		} catch (IllegalArgumentException e) {
			throw new ValueOfException(e);
		} catch (NoSuchMethodException e) {
			throw new ValueOfException(e);
		} catch (IllegalAccessException e) {
			throw new ValueOfException(e);
		} catch (InvocationTargetException e) {
			throw new ValueOfException(e);
		}
	}

}  