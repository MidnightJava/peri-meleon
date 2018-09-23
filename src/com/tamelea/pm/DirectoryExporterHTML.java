package com.tamelea.pm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.PMString;
import com.tamelea.pm.data.Phone;

final class DirectoryExporterHTML {
	private Data data;
	private List<MemberIndex> members;
	private PrintStream ps;
	
	DirectoryExporterHTML(Data data, PrintStream ps) {
		this.data = data;
		this.ps = ps;
		this.members = data.getDirectoryMembers();
	}

	void export()
	throws IOException
	{
		writeHtml();
	}
	
	private void writeHtml()
	throws IOException
	{
		println(0, "<!DOCTYPE html");
		println(0, "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
		println(0, "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		println(0, "<html>");
		writeHead(1);
		writeBody(1);
		ps.println("</html>");
	}
	
	private void writeHead(int indent)
	throws IOException
	{
		LineNumberReader lnr = null;
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("com/tamelea/pm/directory.css");
			if (is == null) throw new IllegalStateException("Can't find resource directory.css");
			lnr = new LineNumberReader(new InputStreamReader(is));
			println(indent, "<head>");
			println(indent + 1, "<title>Directory</title>");
			println(indent + 1, "<style type=\"text/css\">");
			while (true) {
				String cssLine = lnr.readLine();
				if (cssLine == null) break;
				println(indent + 2, cssLine);
			}
			println(indent + 1, "</style>");
			println(indent, "</head>");
		} finally {
			if (lnr != null) lnr.close();
		}
	}
	
	private void writeBody(int indent) {
		println(indent, "<body>");
		for (MemberIndex member : members) {
			HouseholdIndex household = (HouseholdIndex)data.getMemberValue(member, MemberField.HOUSEHOLD);
			if (household != null) {
				MemberIndex head = (MemberIndex)data.getHouseholdValue(household, HouseholdField.HEAD);
				if (member.equals(head)) writeHousehold(indent + 1, household);
			}
			AddressIndex tempAddress = (AddressIndex)data.getMemberValue(member, MemberField.TEMP_ADDRESS);
			if (tempAddress != null) writeTempAddress(indent + 1, member, tempAddress);
		}
		println(indent, "</body>");
	}
	
	private void writeHousehold(int indent, HouseholdIndex household) {
		println(indent, "<div class=\"household\">");
		MemberIndex head = (MemberIndex)data.getHouseholdValue(household, HouseholdField.HEAD);
		PMString headLastName = new PMString("");
		if (head != null) {
			headLastName = (PMString)data.getMemberValue(head, MemberField.LAST_NAME);
			writeHeadAndSpouse(indent + 1, household, head, headLastName);
			writeOthers(indent + 1, household, headLastName);
		}
		AddressIndex address = (AddressIndex)data.getHouseholdValue(household, HouseholdField.ADDRESS);
		writeAddress(indent + 1, address);
		writeHomePhone(indent + 1, address);
		if (head != null) writeMember(indent + 1, head);
		MemberIndex spouse = (MemberIndex)data.getHouseholdValue(household, HouseholdField.SPOUSE);
		if (spouse != null) writeMember(indent + 1, spouse);
		List<MemberIndex> children = data.getChildrenByAge(household);
		for (MemberIndex child : children) writeMember(indent + 1, child);
		println(indent, "</div>");
	}
	
	private void writeHeadAndSpouse(
			int indent, 
			HouseholdIndex household, 
			MemberIndex head, 
			PMString headLastName) 
	{
		String remainder = ", " + data.getInformalFrstName(head);
		MemberIndex spouse = (MemberIndex)data.getHouseholdValue(household, HouseholdField.SPOUSE);
		if (spouse != null) {
			String spouseFirst = data.getInformalFrstName(spouse);
			remainder += " & " + spouseFirst;
			remainder += lastNameIfDifferent(headLastName, spouse);
		}
		println(indent, "<div class=\"headandspouse\">");
		println(indent + 1, "<font class=\"emphasis\">" + headLastName + "</font>"
				+ remainder + ".");
		println(indent, "</div>");
	}
	
	private void writeOthers(int indent, HouseholdIndex household, PMString headLastName) {
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
			println(indent, "<div class=\"others\">" + others + "</div>");
		}
	}
	
	private void writeAddress(int indent, AddressIndex addressIndex) {
		String address = ((PMString)data.getAddressValue(addressIndex, AddressField.ADDRESS)).toString();
		String address2 = ((PMString)data.getAddressValue(addressIndex, AddressField.ADDRESS_2)).toString();
		if (address2.length() > 0) address += " / " + address2;
		address += ", " + ((PMString)data.getAddressValue(addressIndex, AddressField.CITY)).toString();
		address += ", " + ((PMString)data.getAddressValue(addressIndex, AddressField.STATE)).toString();
		address += " " + ((PMString)data.getAddressValue(addressIndex, AddressField.POSTAL_CODE)).toString();
		String country = ((PMString)data.getAddressValue(addressIndex, AddressField.COUNTRY)).toString();
		if (country.length() > 0) address += " " + country;
		println(indent , "<div class=\"address\">" + address + "</div>");
	}
	
	private void writeHomePhone(int indent, AddressIndex address) {
		String phone = ((Phone)data.getAddressValue(address, AddressField.HOME_PHONE)).toString();
		println(indent , "<div class=\"homephone\">" + phone + "</div>");
	}
	
	private void writeMember(int indent, MemberIndex member) {
		ArrayList<String> entries = new ArrayList<String>();
		String eMail = ((PMString)data.getMemberValue(member, MemberField.EMAIL)).toString();
		if (eMail.length() > 0) entries.add(eMail);
		String workEMail = ((PMString)data.getMemberValue(member, MemberField.WORK_EMAIL)).toString();
		if (workEMail.length() > 0) entries.add(workEMail + "(w)");
		String mobilePhone = ((Phone)data.getMemberValue(member, MemberField.MOBILE_PHONE)).toString();
		if (mobilePhone.length() > 0) entries.add(mobilePhone + "(m)");
		String workPhone = ((Phone)data.getMemberValue(member, MemberField.WORK_PHONE)).toString();
		if (workPhone.length() > 0) entries.add(workPhone + "(w)");
		if (entries.isEmpty()) return;
		String stuff = data.getInformalFrstName(member) + ":";
		stuff += " " + entries.get(0);
		for (int i = 1; i < entries.size(); ++i) {
			stuff += " | " + entries.get(i);
		}
		println(indent, "<div class=\"member\">" + stuff + "</div>");
	}
	
	private void writeTempAddress(int indent, MemberIndex member, AddressIndex addressIndex) {
		println(indent, "<div class=\"household\">");
		PMString memberLastName = (PMString)data.getMemberValue(member, MemberField.LAST_NAME);
		String remainder = ", " + data.getInformalFrstName(member);
		println(indent, "<div class=\"headandspouse\">");
		println(indent + 1, "<font class=\"emphasis\">" + memberLastName + "</font>"
				+ remainder + ".");
		println(indent, "</div>");
		writeAddress(indent + 1, addressIndex);
		writeHomePhone(indent + 1, addressIndex);
		writeMember(indent + 1, member);
		println(indent, "</div>");
	}
	
	private String lastNameIfDifferent(PMString headLastName, MemberIndex member) {
		PMString otherLastName = (PMString)data.getMemberValue(
				member, MemberField.LAST_NAME);
		if (!otherLastName.equals(headLastName)) return " " + otherLastName;
		return "";
	}
	
	private void println(int indent, String s) {
		for (int i = 0; i < indent; ++i) ps.print("\t");
		ps.println(s);
	}
}
