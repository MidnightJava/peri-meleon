package com.tamelea.pm.data;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Parameters for enum definitions:
 * field name
 * displayname (used for table headings, etc.)
 * field class (assumed to implement static valueof(String)
 * editor class
 * whether visible in editor or table
 *
 */
public enum MemberField implements DataField {
	INDEX			("index",			"Index",				MemberIndex.class,		null,						false),
	LAST_NAME		("lastName",		"Last Name",			PMString.class,			RequiredStringEditor.class,	true),
	FIRST_NAME		("firstName",		"First Name",			PMString.class,			RequiredStringEditor.class,	true),
	MIDDLE_NAME		("middleName",		"Middle Name",			PMString.class,			StringEditor.class,			true),
	MAIDEN_NAME		("maidenName",		"Maiden Name",			PMString.class,			StringEditor.class,			true),
	SUFFIX			("suffix",			"Suffix",				PMString.class,			StringEditor.class,			true),
	TITLE			("title",			"Title",				PMString.class,			StringEditor.class,			true),
	NICK_NAME		("nickName",		"Nickname",				PMString.class,			StringEditor.class,			true),
	HOUSEHOLD		("household",		"Household",			HouseholdIndex.class,	HouseholdEditor.class,		true),
	TEMP_ADDRESS	("tempAddress",		"Temp Address",			AddressIndex.class,		AddressEditor.class,		true),
	EMAIL			("eMail",			"E-mail",				PMString.class,			StringEditor.class,			true),
	WORK_EMAIL		("workEMail",		"Work E-mail",			PMString.class,			StringEditor.class,			true),
	MOBILE_PHONE	("mobilePhone",		"Mobile Phone",			Phone.class,			PhoneEditor.class,			true),
	WORK_PHONE		("workPhone",		"Work Phone",			Phone.class,			PhoneEditor.class,			true),
	EX_DIRECTORY	("exDirectory",		"Exclude from Dir",		Boolean.class,			BooleanEditor.class,		true),
	STATUS			("status",			"Status",				MemberStatus.class,		MemberStatusEditor.class,	true),
	RESIDENT		("resident",		"Resident",				Boolean.class,			BooleanEditor.class,		true),
	BAPTISM			("baptism",			"Baptism",				PMString.class,			StringEditor.class,			true),
	SEX				("sex",				"Sex",					Sex.class,				SexEditor.class,			true),
	FATHER			("father",			"Father",				MemberIndex.class,		MaleMemberEditor.class,			true),
	MOTHER			("mother",			"Mother",				MemberIndex.class,		FemaleMemberEditor.class,			true),
	DATE_OF_BIRTH	("dateOfBirth",		"DOB (mm/dd/yyyy)",		PMDate.class,			DateEditor.class,			true),
	PLACE_OF_BIRTH	("placeOfBirth",	"Place of Birth",		PMString.class,			StringEditor.class,			true),
	MARITAL_STATUS	("maritalStatus",	"Marital Status",		MaritalStatus.class,	MaritalStatusEditor.class,	true),
	SPOUSE			("spouse",			"Spouse",				PMString.class,			StringEditor.class,			true),
	DATE_OF_MARR	("dateOfMarriage",	"Date Marr (mm/dd/yyyy)",PMDate.class,			DateEditor.class,			true),
	DIVORCE			("divorce",			"Divorce",				PMString.class,			StringEditor.class,			true),
	EDUCATION		("education",		"Education",			PMString.class,			StringEditor.class,			true),
	EMPLOYER		("employer",		"Employer",				PMString.class,			StringEditor.class,			true),
	LAST_CHANGE		("dateLastChange",	"Date Last Change",		PMDate.class,			DateEditor.class,			true);
	
	public final Field field;
	public final String displayName;
	public final Class<?> fieldClass;
	public final Class<?> editorClass;
	public final boolean editable;
	private static HashMap<String,MemberField> paramMap;
	
	static {
		paramMap = new HashMap<String,MemberField>();
		paramMap.put("Index", MemberField.INDEX);
		paramMap.put("Last Name", MemberField.LAST_NAME);
		paramMap.put("First Name", MemberField.FIRST_NAME);
		paramMap.put("Middle Name", MemberField.MIDDLE_NAME);
		paramMap.put("Maiden Name", MemberField.MAIDEN_NAME);
		paramMap.put("Suffix", MemberField.SUFFIX);
		paramMap.put("Title", MemberField.TITLE);
		paramMap.put("Nickname", MemberField.NICK_NAME);
		paramMap.put("Date Last Change", MemberField.LAST_CHANGE);
		paramMap.put("Sex", MemberField.SEX);
		paramMap.put("DOB (mm/dd/yyyy)", MemberField.DATE_OF_BIRTH);
		paramMap.put("Place of Birth", MemberField.PLACE_OF_BIRTH);
		paramMap.put("Status", MemberField.STATUS);
		paramMap.put("Exclude from Dir", MemberField.EX_DIRECTORY);
		paramMap.put("Resident", MemberField.RESIDENT);
		paramMap.put("Household", MemberField.HOUSEHOLD);
		paramMap.put("Temp Address", MemberField.TEMP_ADDRESS);
		paramMap.put("Marital Status", MemberField.MARITAL_STATUS);
		paramMap.put("Spouse", MemberField.SPOUSE);
		paramMap.put("Date Marr (mm/dd/yyyy)", MemberField.DATE_OF_MARR);
		paramMap.put("Father", MemberField.FATHER);
		paramMap.put("Mother", MemberField.MOTHER);
		paramMap.put("E-mail", MemberField.EMAIL);
		paramMap.put("Work E-mail", MemberField.WORK_EMAIL);
		paramMap.put("Mobile Phone", MemberField.MOBILE_PHONE);
		paramMap.put("Work Phone", MemberField.WORK_PHONE);
		paramMap.put("Education", MemberField.EDUCATION);
		paramMap.put("Employer", MemberField.EMPLOYER);
		paramMap.put("Baptism", MemberField.BAPTISM);
		paramMap.put("Divorce", MemberField.DIVORCE);
		paramMap.put("Name", null);
	}
	
	MemberField(String name, String displayName, Class<?> fieldClass, Class<?> editorClass, boolean editable) {
		try {
			this.field = Member.class.getDeclaredField(name);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("MemberField unknown field name " + name);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("MemberField unknown field name " + name);
		}
		this.displayName = displayName;
		this.fieldClass = fieldClass;
		this.editorClass = editorClass;
		this.editable = editable;
	}
	
	public static MemberField getConstantValue(String displayName){
		return paramMap.get(displayName);
	}
}
