package com.tamelea.pm.nhpc;

import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tamelea.pm.csv.LineParser;
import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.HouseholdNameComparator;
import com.tamelea.pm.data.MaritalStatus;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.MemberStatus;
import com.tamelea.pm.data.PMDate;
import com.tamelea.pm.data.PMString;
import com.tamelea.pm.data.Phone;
import com.tamelea.pm.data.Sex;
import com.tamelea.pm.data.Transaction;
import com.tamelea.pm.data.TransactionField;
import com.tamelea.pm.data.TransactionType;

public final class NHPCImporter {
	private static Data data;
	private static EnumMap<NHPCField, Integer> fields;
	private static List<String> values;
	private static HashMap<String, HouseholdIndex> households;

	public static void read(Data theData, LineNumberReader reader) 
	throws NHPCImportException 
	{
		data = theData;
		fields = new EnumMap<NHPCField, Integer>(NHPCField.class);
		households = new HashMap<String, HouseholdIndex>();
		try {
			//Determine order of fields
			String line = reader.readLine();
			if (line == null) throw new NHPCImportException(1, "No first line!", null);
			determineFieldOrder(line);
			while ((line = reader.readLine()) != null) {
				values = parseValues(line);
				processMember();
			}
			chooseHeads();
		} catch (IOException e) {
			throw new NHPCImportException(reader.getLineNumber() + 1, e.getMessage(), e);
		} catch (ParseException e) {
			throw new NHPCImportException(reader.getLineNumber() + 1, e.getMessage(), e);
		} catch (NHPCParseException e) {
			throw new NHPCImportException(reader.getLineNumber() + 1, e.getMessage(), e);
		}
	}
	
	private static void determineFieldOrder(String line)  
	throws NHPCImportException 
	{
		LineParser parser = new LineParser(line);
		int position = 0;
		while (parser.hasNext()) {
			String fieldName = parser.next();
			NHPCField field = NHPCField.valueOf(fieldName);
			if (field == null) throw new NHPCImportException(1, "Unknown field " + fieldName, null);
			fields.put(field, position++);
		}
	}
	
	private static List<String> parseValues(String line) {
		ArrayList<String> result = new ArrayList<String>();
		LineParser parser = new LineParser(line);
		while (parser.hasNext()) {
			result.add(parser.next());
		}
		return result;
	}
	
	/**
	 * The point of this little gem is that the values list might not be complete,
	 * as the CSV might not have entries for everything at the end.
	 * So if the dreaded exception is thrown, return something harmless.
	 * The OO design is breaking down here...
	 * @param i
	 * @return
	 */
	private static String getValue(int i) {
		String result = "";
		try {
			result = values.get(i);
		} catch (IndexOutOfBoundsException fallThrough) { }
		return result;
	}
	
	private static void processMember() 
	throws ParseException, NHPCParseException
	{
		MemberIndex member = data.addMember();
		data.setMemberValue(member, MemberField.LAST_NAME, new PMString(getValue(fields.get(NHPCField.LAST_NAME))));
		data.setMemberValue(member, MemberField.FIRST_NAME, new PMString(getValue(fields.get(NHPCField.FIRST_NAME))));
		data.setMemberValue(member, MemberField.SUFFIX, new PMString(getValue(fields.get(NHPCField.SUFFIX))));
		String sexString = getValue(fields.get(NHPCField.SEX));
		if (sexString.equals("m")) data.setMemberValue(member, MemberField.SEX, Sex.MALE);
		else data.setMemberValue(member, MemberField.SEX, Sex.FEMALE);
		data.setMemberValue(member, MemberField.NICK_NAME, new PMString(getValue(fields.get(NHPCField.NICKNAME))));
		data.setMemberValue(member, MemberField.DATE_OF_BIRTH, new PMDate(getValue(fields.get(NHPCField.DATE_OF_BIRTH))));
		determineStatus(member, values);
		PMDate lastChangeDate = new PMDate(getValue(fields.get(NHPCField.LAST_CHANGE_DATE)));
		if (lastChangeDate.isEmpty()) throw new NHPCParseException("Empty last change date", null);
		data.setMemberValue(member, MemberField.LAST_CHANGE, lastChangeDate);
		data.setMemberValue(member, MemberField.MIDDLE_NAME, new PMString(getValue(fields.get(NHPCField.MIDDLE_NAME))));
		data.setMemberValue(member, MemberField.MAIDEN_NAME, new PMString(getValue(fields.get(NHPCField.MAIDEN_NAME))));
		HouseholdIndex household = chooseHousehold(member);
		data.setMemberValue(member, MemberField.HOUSEHOLD, household);
		String workPhone = getValue(fields.get(NHPCField.WORK_PHONE));
		data.setMemberValue(member, MemberField.WORK_PHONE, formatPhone(workPhone));
		String mobilePhone = getValue(fields.get(NHPCField.MOBILE_PHONE));
		data.setMemberValue(member, MemberField.MOBILE_PHONE, formatPhone(mobilePhone));
		data.setMemberValue(member, MemberField.TITLE, new PMString(getValue(fields.get(NHPCField.TITLE))));
		data.setMemberValue(member, MemberField.PLACE_OF_BIRTH, new PMString(getValue(fields.get(NHPCField.PLACE_OF_BIRTH))));
		data.setMemberValue(member, MemberField.BAPTISM, new PMString(getValue(fields.get(NHPCField.BAPTISM))));
//		data.setMemberValue(member, MemberField.PROF_FAITH, new PMString(getValue(fields.get(NHPCField.PROF_FAITH))));
		cullProfession(member);
		data.setMemberValue(member, MemberField.DATE_OF_MARR, new PMDate(getValue(fields.get(NHPCField.DATE_OF_MARR))));
		PMString spouse = new PMString(getValue(fields.get(NHPCField.SPOUSE)));
		data.setMemberValue(member, MemberField.SPOUSE, spouse);
		//no divorced persons, as it happens
		data.setMemberValue(member, MemberField.MARITAL_STATUS, (spouse.length() > 0) ? MaritalStatus.MARRIED : MaritalStatus.SINGLE);
		data.setMemberValue(member, MemberField.DIVORCE, new PMString(getValue(fields.get(NHPCField.DIVORCE))));
		data.setMemberValue(member, MemberField.EDUCATION, new PMString(getValue(fields.get(NHPCField.EDUCATION))));
		data.setMemberValue(member, MemberField.EMPLOYER, new PMString(getValue(fields.get(NHPCField.EMPLOYER))));
		PMString eMail = new PMString(getValue(fields.get(NHPCField.EMAIL)));
		PMString workEMail = new PMString(getValue(fields.get(NHPCField.WORK_EMAIL)));
		if (eMail.length() < 1 && workEMail.length() > 0) {
			eMail = workEMail;
			workEMail = new PMString("");
		}
		data.setMemberValue(member, MemberField.EMAIL, eMail);
		data.setMemberValue(member, MemberField.WORK_EMAIL, workEMail);
	}
	
	private static void determineStatus(MemberIndex member, List<String>values)
	throws ParseException, NHPCParseException
	{
		String statusString = getValue(fields.get(NHPCField.STATUS));
		if (statusString.equals("visitor")) {
			data.setMemberValue(member, MemberField.STATUS, MemberStatus.VISITOR);
		} else if (statusString.equals("Pastor")) {
			data.setMemberValue(member, MemberField.STATUS, MemberStatus.PASTOR);
		} else {
			//All members originally were received
			PMDate dateReceived = new PMDate(getValue(fields.get(NHPCField.DATE_RECEIVED)));
			if (dateReceived == null) throw new NHPCParseException("date received is missing", null);
			Transaction transaction = data.addTransaction(member);
			data.setTransactionValue(transaction, TransactionField.DATE, dateReceived);
			data.setTransactionValue(transaction, TransactionField.TYPE, TransactionType.RECEIVED);
			PMString prevMembership = new PMString(getValue(fields.get(NHPCField.PREV_MEMBERSHIP)));
			data.setTransactionValue(transaction, TransactionField.CHURCH, prevMembership);
			if (statusString.equals("z. administrative removal")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.REMOVED);
				//We'll have to add a transaction by hand
			} else if (statusString.equals("dismissed")) {
				PMDate dateDismissed = new PMDate(getValue(fields.get(NHPCField.DATE_DISMISSED)));
				if (dateDismissed.isEmpty()) {
					data.setMemberValue(member, MemberField.STATUS, MemberStatus.DISMISSAL_PENDING);
					transaction = data.addTransaction(member);
					PMDate lastChangeDate = new PMDate(getValue(fields.get(NHPCField.LAST_CHANGE_DATE)));
					data.setTransactionValue(transaction, TransactionField.DATE, lastChangeDate);
					data.setTransactionValue(transaction, TransactionField.TYPE, TransactionType.DISMISSAL_PENDING);
				} else {
					data.setMemberValue(member, MemberField.STATUS, MemberStatus.DISMISSED);
					transaction = data.addTransaction(member);
					data.setTransactionValue(transaction, TransactionField.DATE, dateDismissed);
					data.setTransactionValue(transaction, TransactionField.TYPE, TransactionType.DISMISSED);
					PMString dismissedTo = new PMString(getValue(fields.get(NHPCField.DISMISSED_TO)));
					data.setTransactionValue(transaction, TransactionField.CHURCH, dismissedTo);
				}
			} else if (statusString.equals("communicant-adult")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.COMMUNING);
				data.setMemberValue(member, MemberField.RESIDENT, new Boolean(true));
			} else if (statusString.equals("excommunicated")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.EXCOMMUNICATED);
				//We'll have to add a transaction by hand
			} else if (statusString.equals("non-res. com.")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.COMMUNING);
				data.setMemberValue(member, MemberField.RESIDENT, new Boolean(false));
			} else if (statusString.equals("communicant-minor")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.COMMUNING);
				data.setMemberValue(member, MemberField.RESIDENT, new Boolean(true));
			} else if (statusString.equals("non-res. non-com.")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.NONCOMMUNING);
				data.setMemberValue(member, MemberField.RESIDENT, new Boolean(false));
			} else if (statusString.equals("non-communicant")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.NONCOMMUNING);
				data.setMemberValue(member, MemberField.RESIDENT, new Boolean(true));
			} else if (statusString.equals("deceased")) {
				data.setMemberValue(member, MemberField.STATUS, MemberStatus.DEAD);
				PMDate dateOfDeath = new PMDate(getValue(fields.get(NHPCField.DATE_OF_DEATH)));
				transaction = data.addTransaction(member);
				data.setTransactionValue(transaction, TransactionField.DATE, dateOfDeath);
				data.setTransactionValue(transaction, TransactionField.TYPE, TransactionType.DIED);
			} else {
				throw new ParseException("Unrecognized imported status '" + statusString + "'", 0);
			}
		}
	}
	
	private static final DateFormatRecord[] formatRecords = new DateFormatRecord[] {
		new DateFormatRecord("MM/dd/yyyy",	"\\d\\d/\\d\\d/\\d\\d\\d\\d"),
		new DateFormatRecord("MM/d/yyyy",	"\\d\\d/\\d/\\d\\d\\d\\d"),
		new DateFormatRecord("M/dd/yyyy",	"\\d/\\d\\d/\\d\\d\\d\\d"),
		new DateFormatRecord("MM/dd/yy",	"\\d\\d/\\d\\d/\\d\\d"),
		new DateFormatRecord("MM/d/yy",		"\\d\\d/\\d/\\d\\d"),
		new DateFormatRecord("M/dd/yy",		"\\d/\\d\\d/\\d\\d"),
		new DateFormatRecord("M/d/yy",		"\\d/\\d/\\d\\d"),
		new DateFormatRecord("MM/yy",		"\\d\\d/\\d\\d"),
		new DateFormatRecord("M/yy",		"\\d/\\d\\d"),
		new DateFormatRecord("yyyy",		"\\d\\d\\d\\d"),
	};
	
	private static void cullProfession(MemberIndex member) {
		String profession = getValue(fields.get(NHPCField.PROF_FAITH));
		PMDate professionDate = null;
		if (profession.length() < 1) return;
		for (DateFormatRecord record : formatRecords) {
			professionDate = cullDate(record.format, record.pattern, profession);
			if (professionDate != null) break;
		}
		Transaction transaction = data.addTransaction(member);
		data.setTransactionValue(transaction, TransactionField.DATE, professionDate);
		data.setTransactionValue(transaction, TransactionField.TYPE, TransactionType.PROFESSION);
		data.setTransactionValue(transaction, TransactionField.COMMENT, new PMString(profession));
	}
	
	private static PMDate cullDate(DateFormat format, Pattern pattern, String string) {
		PMDate result = null;
		Matcher matcher = pattern.matcher(string);
		try {
			if (matcher.find()) {
				result = new PMDate(format, string.substring(matcher.start(), matcher.end()));
			}
		} catch (ParseException acceptNull) { }
		return result;
	}
	
	private static HouseholdIndex chooseHousehold(MemberIndex member)
	throws ParseException, NHPCParseException
	{
		String head = getValue(fields.get(NHPCField.HOUSEHOLD_HEAD));
		HouseholdIndex hi = households.get(head);
		if (hi == null) {
			hi = data.addHousehold();
			households.put(head, hi);
			data.setHouseholdValue(hi, HouseholdField.INDEX, hi);
			data.setHouseholdValue(hi, HouseholdField.NAME, new PMString(head));
			AddressIndex ai = data.addAddress();
			data.setHouseholdValue(hi, HouseholdField.ADDRESS, ai);
			//during import there's a 1-1 correspondence of household to address
			data.setAddressValue(ai, AddressField.ADDRESS, new PMString(getValue(fields.get(NHPCField.ADDRESS))));
			data.setAddressValue(ai, AddressField.ADDRESS_2, new PMString(getValue(fields.get(NHPCField.ADDRESS_2))));
			data.setAddressValue(ai, AddressField.CITY, new PMString(getValue(fields.get(NHPCField.CITY))));
			data.setAddressValue(ai, AddressField.STATE, new PMString(getValue(fields.get(NHPCField.STATE))));
			data.setAddressValue(ai, AddressField.POSTAL_CODE, new PMString(getValue(fields.get(NHPCField.POSTAL_CODE))));
			data.setAddressValue(ai, AddressField.COUNTRY, new PMString(getValue(fields.get(NHPCField.COUNTRY))));
			//this is bogus and will need to be fixed: NHPC doesn't have a family, as opp to, personal email
			//OK, we fix it by not recording a family e-mail: American individualism wins out!
//			data.setHouseholdValue(hi, HouseholdField.EMAIL, new PMString(getValue(fields.get(NHPCField.EMAIL))));
			data.setAddressValue(ai, AddressField.HOME_PHONE, formatPhone(getValue(fields.get(NHPCField.HOME_PHONE))));
		}
		//put 'em all in 'other' for now
		data.setOtherMembership(hi, member, true);
		return hi;
	}
	
	private final static Pattern p1 = Pattern.compile("\\(\\d\\d\\d\\) \\d\\d\\d\\-\\d\\d\\d\\d");
	private final static Pattern p2 = Pattern.compile("\\d\\d\\d\\-\\d\\d\\d\\-\\d\\d\\d\\d");
	private final static Pattern p3 = Pattern.compile("\\d\\d\\d\\.\\d\\d\\d\\.\\d\\d\\d\\d");
	
	private static Phone formatPhone(String in) {
		String out = "";
		if (p1.matcher(in).find()) {
			out = in;
		} else if (p2.matcher(in).find()) {
			out = "(" + in.substring(0, 3) + ") " + in.substring(4, 7) + "-" + in.substring(8, 12);
		} else if (p3.matcher(in).find()) {
			out = "(" + in.substring(0, 3) + ") " + in.substring(4, 7) + "-" + in.substring(8, 12);
		} else {
			out = in; //we give up!
		}
		return new Phone(out);
	}
	
	/**
	 * Guess at identities of HEAD and SPOUSE in each household.
	 * This will fail for a single parent!
	 *
	 */
	private static void chooseHeads() {
		List<HouseholdIndex> householdsByName = data.queryHouseholds(null, new HouseholdNameComparator(data));
		for (HouseholdIndex household : householdsByName) {
			//if (household == null) continue; //there's a null value at front!
			List<MemberIndex> inhabitants = data.getInhabitantsByAge(household);
			if (inhabitants.size() == 1) {
				data.setHouseholdValue(household, HouseholdField.HEAD, inhabitants.get(0));
				data.setOtherMembershipWithoutFixup(household, inhabitants.get(0), false);
			} else if (inhabitants.size() > 1) {
				MemberIndex oldest = inhabitants.get(0);
				MemberIndex nextOldest = inhabitants.get(1);
				if ((Sex)data.getMemberValue(oldest, MemberField.SEX) == Sex.MALE) {
					data.setHouseholdValue(household, HouseholdField.HEAD, oldest);
					data.setHouseholdValue(household, HouseholdField.SPOUSE, nextOldest);
				} else {
					data.setHouseholdValue(household, HouseholdField.HEAD, nextOldest);
					data.setHouseholdValue(household, HouseholdField.SPOUSE, oldest);
				}
				data.setOtherMembershipWithoutFixup(household, inhabitants.get(0), false);
				data.setOtherMembershipWithoutFixup(household, inhabitants.get(1), false);
			}
		}
	}
}
