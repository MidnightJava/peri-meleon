package com.tamelea.pm.data;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class PhoneListMaker {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//The fields to be included 
	private Field[]							fields;
	private Data							data;
	private PrintStream						out;
	private int								minimumAge;
	private String							categories;

	public PhoneListMaker(Data data, PrintStream out, int minimumAge) {
		this.data = data;
		this.out = out;
		this.minimumAge = minimumAge;
		this.fields = new Field[] {
			new Field(MemberField.LAST_NAME,	"Last Name"),
			new Field(ComputedField.NAME,		"First Name"),
			new Field(ComputedField.DISPLAY_NAME,"Display Name"),
			new Field(AddressField.ADDRESS,		"Home Street"),
			new Field(AddressField.ADDRESS_2,	"Home Street 2"),
			new Field(AddressField.CITY,		"Home City"),
			new Field(AddressField.STATE,		"Home State"),
			new Field(AddressField.POSTAL_CODE,	"Home Postal Code"),
			new Field(AddressField.COUNTRY,		"Home Country"),
			new Field(AddressField.HOME_PHONE,	"Home Phone"),
			new Field(MemberField.EMAIL,		"E-mail Address"),
			new Field(MemberField.WORK_EMAIL,	"E-mail 2 Address"),
			new Field(MemberField.MOBILE_PHONE,	"Mobile Phone"),
			new Field(MemberField.WORK_PHONE,	"Business Phone"),
			new Field(ComputedField.CATEGORIES,	"Categories"),
		};
	}
	
	public void make() {
		categories =  "church-" + dateFormat.format(new Date());
		//write first line
		ArrayList<String> values = new ArrayList<String>();
		for (Field field : fields) {
			values.add(field.headerName);
		}
		writeLine(values);
		List<MemberIndex> members = data.queryMembers(
				new OlderActiveMemberFilter(data, minimumAge), 
				new InformalNameComparator(data));
		for (MemberIndex member : members) {
			boolean excludedFromDirectory = (Boolean)data.getMemberValue(member, MemberField.EX_DIRECTORY);
			if (!excludedFromDirectory) writeMember(member);
		}
	}
	
	private void writeMember(MemberIndex member) {
		HouseholdIndex household = (HouseholdIndex)data.getMemberValue(member, MemberField.HOUSEHOLD);
		AddressIndex address = (AddressIndex)data.getHouseholdValue(household, HouseholdField.ADDRESS);
		AddressIndex tempAddress = (AddressIndex)data.getMemberValue(member, MemberField.TEMP_ADDRESS);
		//If temp address, use it instead of home address
		if (tempAddress != null) address = tempAddress;
		ArrayList<String> values = new ArrayList<String>();
		for (Field field : fields) {
			if (field.kind instanceof MemberField) {
				Object value = data.getMemberValue(member, (MemberField)field.kind);
				if (value == null) values.add("");
				else values.add(value.toString());
			} else if (field.kind instanceof HouseholdField) {
				if (household != null) {
					Object value = data.getHouseholdValue(household, (HouseholdField)field.kind);
					if (value == null) values.add("");
					else values.add(value.toString());
				} else values.add("");
			} else if (field.kind instanceof AddressField) {
				if (household != null) {
					Object value = data.getAddressValue(address, (AddressField)field.kind);
					if (value == null) values.add("");
					else values.add(value.toString());
				} else values.add("");
			} else if (field.kind instanceof ComputedField) {
				switch ((ComputedField)field.kind) {
				case CATEGORIES:
					values.add(categories);
					break;
				case NAME:
					values.add(data.getInformalFrstName(member));
					break;
				case DISPLAY_NAME:
					PMString lastName = (PMString)data.getMemberValue(member, MemberField.LAST_NAME);
					values.add(lastName.toString() + ", " + data.getInformalFrstName(member));
					break;
				}
			}
		}
		writeLine(values);
	}
	
	private void writeLine(List<String>values) {
		writeValue(values.get(0));
		for (int i = 1; i < values.size(); ++i) {
			out.print(",");
			writeValue(values.get(i));
		}
		out.println();
	}
	
	private void writeValue(String value) {
		if (value == null) return;
		if (value.indexOf(',') < 0) out.print(value);
		else out.print("\"" + value + "\"");
	}
	
	/**
	 * A marker to tell PhoneListMaker to insert informal name.
	 *
	 */
	private enum ComputedField implements DataField {
		NAME,
		DISPLAY_NAME,
		CATEGORIES,
	}
	
	private final class Field {
		DataField kind;
		String headerName;
		
		Field(DataField kind, String headerName) {
			this.kind = kind;
			this.headerName = headerName;
		}
	}
}
