package com.tamelea.pm;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.PMString;
import com.tamelea.pm.data.Phone;
import com.tamelea.pm.xml.Renderer;
import com.tamelea.pm.xml.RendererException;
import com.tamelea.pm.xml.Util;

final class DirectoryExporterPDF {
	private Data data;
	private List<MemberIndex> members;
	private FileOutputStream fos;
	
	DirectoryExporterPDF(Data data, FileOutputStream fos) {
		this.data = data;
		this.fos = fos;
		this.members = data.getDirectoryMembers();
	}

	void export()
	throws RendererException
	{
		Document directory = makeDirectoryDocument();
		Util.dumpDocument(directory, System.out);
		Renderer.getRenderer().render(directory, "/Library/Application Support/com.tamelea.perimeleon/resource/directory-pdf.xsl", fos);
	}
	
	private Document makeDirectoryDocument() {
		Document document = DocumentHelper.createDocument();
		document.addComment("Directory from " + PeriMeleon.version + " written " + new Date());
		Element root = document.addElement("directory");
		root.addAttribute("date", PeriMeleon.elegantFormat.format(new Date()));
		for (MemberIndex member : members) {
			if (isExcluded(member)) continue;
			HouseholdIndex household = (HouseholdIndex)data.getMemberValue(member, MemberField.HOUSEHOLD);
			if (household != null) {
				MemberIndex head = (MemberIndex)data.getHouseholdValue(household, HouseholdField.HEAD);
				if (member.equals(head) && !isExcluded(member)) addHouseholdElement(root, household);
			}
			AddressIndex tempAddress = (AddressIndex)data.getMemberValue(member, MemberField.TEMP_ADDRESS);
			if (tempAddress != null) addTempAddressElements(root, member, tempAddress);
		}
		return document;
	}
	
	private void addHouseholdElement(Element parent, HouseholdIndex household) {
		Element householdElement = new DefaultElement("household");
		parent.add(householdElement);
		MemberIndex head = (MemberIndex)data.getHouseholdValue(household, HouseholdField.HEAD);
		PMString headLastName = new PMString("");
		if (head != null) {
			headLastName = (PMString)data.getMemberValue(head, MemberField.LAST_NAME);
			addHeadAndSpouseElement(householdElement, household, head, headLastName);
			addOthersElement(householdElement, household, headLastName);
		}
		AddressIndex address = (AddressIndex)data.getHouseholdValue(household, HouseholdField.ADDRESS);
		addAddressElements(householdElement, address);
		addHomePhoneElement(householdElement, address);
		if (head != null) addMemberElements(householdElement, head);
		MemberIndex spouse = (MemberIndex)data.getHouseholdValue(household, HouseholdField.SPOUSE);
		if (spouse != null) addMemberElements(householdElement, spouse);
		List<MemberIndex> children = data.getChildrenByAge(household);
		for (MemberIndex child : children) addMemberElements(householdElement, child);
	}
	
	private void addHeadAndSpouseElement(
			Element parent, 
			HouseholdIndex household, 
			MemberIndex head, PMString 
			headLastName)
	{
		String remainder = data.getInformalFrstName(head);
		MemberIndex spouse = (MemberIndex)data.getHouseholdValue(household, HouseholdField.SPOUSE);
		if (spouse != null && !isExcluded(spouse)) {
			String spouseFirst = data.getInformalFrstName(spouse);
			remainder += " & " + spouseFirst;
			remainder += lastNameIfDifferent(headLastName, spouse);
		}
		Element familyNameElement = new DefaultElement("family-name");
		familyNameElement.addAttribute("text", headLastName.toString());
		parent.add(familyNameElement);
		Element headAndSpouseElement = new DefaultElement("head-and-spouse");
		headAndSpouseElement.addAttribute("text", remainder);
		parent.add(headAndSpouseElement);
	}
	
	private void addOthersElement(Element parent, HouseholdIndex household, PMString headLastName) {
		String others;
		List<MemberIndex> children = data.getChildrenByAge(household);
		if (children.size() > 0) {
			String childFirst = data.getInformalFrstName(children.get(0));
			others = childFirst;
			others += lastNameIfDifferent(headLastName, children.get(0));
			for (int i = 1; i < children.size(); ++i) {
				childFirst = data.getInformalFrstName(children.get(i));
				others += ", " + childFirst + lastNameIfDifferent(headLastName, children.get(i));
			}
			Element othersElement = new DefaultElement("others");
			othersElement.addAttribute("text", others);
			parent.add(othersElement);
		}
	}
	
	private void addAddressElements(Element parent, AddressIndex addressIndex) {
		String address = ((PMString)data.getAddressValue(addressIndex, AddressField.ADDRESS)).toString();
		if (address.length() > 0) parent.add(new DefaultElement("address").addAttribute("text", address));
		String address2 = ((PMString)data.getAddressValue(addressIndex, AddressField.ADDRESS_2)).toString();
		if (address2.length() > 0)
			parent.add(new DefaultElement("address2").addAttribute("text", address2));
		String city = ((PMString)data.getAddressValue(addressIndex, AddressField.CITY)).toString();
		if (city.length() > 0) parent.add(new DefaultElement("city").addAttribute("text", city));
		String state = ((PMString)data.getAddressValue(addressIndex, AddressField.STATE)).toString();
		if (state.length() > 0) parent.add(new DefaultElement("state").addAttribute("text", state));
		String postal = ((PMString)data.getAddressValue(addressIndex, AddressField.POSTAL_CODE)).toString();
		if (postal.length() > 0) parent.add(new DefaultElement("postal-code").addAttribute("text", postal));
		String country = ((PMString)data.getAddressValue(addressIndex, AddressField.COUNTRY)).toString();
		if (country.length() > 0)
			parent.add(new DefaultElement("country").addAttribute("text", country));
	}
	
	private void addHomePhoneElement(Element parent, AddressIndex address) {
		String phone = ((Phone)data.getAddressValue(address, AddressField.HOME_PHONE)).toString();
		parent.add(new DefaultElement("home-phone").addAttribute("text", phone));
	}
	
	private void addMemberElements(Element parent, MemberIndex member) {
		ArrayList<String> entries = new ArrayList<String>();
		String eMail = ((PMString)data.getMemberValue(member, MemberField.EMAIL)).toString();
		if (eMail.length() > 0) entries.add(eMail);
		String workEMail = ((PMString)data.getMemberValue(member, MemberField.WORK_EMAIL)).toString();
		if (workEMail.length() > 0) entries.add(workEMail + " (w)");
		String mobilePhone = ((Phone)data.getMemberValue(member, MemberField.MOBILE_PHONE)).toString();
		if (mobilePhone.length() > 0) entries.add(mobilePhone + " (m)");
		String workPhone = ((Phone)data.getMemberValue(member, MemberField.WORK_PHONE)).toString();
		if (workPhone.length() > 0) entries.add(workPhone + " (w)");
		if (entries.isEmpty()) return;
		Element memberElement = new DefaultElement("member");
		parent.add(memberElement.addAttribute("text", data.getInformalFrstName(member)));
		memberElement.add(new DefaultElement("datum").addAttribute("text", entries.get(0)));
		for (int i = 1; i < entries.size(); i++) {
			memberElement.add(new DefaultElement("datum").addAttribute("text", " | "));
			memberElement.add(new DefaultElement("datum").addAttribute("text", entries.get(i)));
		}
	}
	
	private void addTempAddressElements(Element parent, MemberIndex member, AddressIndex addressIndex) {
		Element householdElement = new DefaultElement("household");
		parent.add(householdElement);
		PMString memberLastName = (PMString)data.getMemberValue(member, MemberField.LAST_NAME);
		householdElement.add(new DefaultElement("family-name").addAttribute("text", memberLastName.toString()));
		String remainder = data.getInformalFrstName(member);
		householdElement.add(new DefaultElement("head-and-spouse").addAttribute("text", remainder));
		addAddressElements(householdElement, addressIndex);
		addHomePhoneElement(householdElement, addressIndex);
		addMemberElements(householdElement, member);
	}
	
	private String lastNameIfDifferent(PMString headLastName, MemberIndex member) {
		PMString otherLastName = (PMString)data.getMemberValue(
				member, MemberField.LAST_NAME);
		if (!otherLastName.equals(headLastName)) return " " + otherLastName;
		return "";
	}

	private boolean isExcluded(MemberIndex member) {
		return (Boolean)data.getMemberValue(member, MemberField.EX_DIRECTORY);
	}
}