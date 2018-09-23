package com.tamelea.pm.data;

import java.lang.reflect.Field;

/**
 * Parameters for enum definitions:
 * field name
 * displayname (used for table headings, etc.)
 * field class (assumed to implement static valueof(String)
 * editor class
 * whether visible in editor or table
 *
 */
public enum HouseholdField implements DataField {
	INDEX		("index",		"Index",		HouseholdIndex.class,	null,						false),
	NAME		("name",		"Name",			PMString.class,			RequiredStringEditor.class,	true),
	HEAD		("head",		"Head",			MemberIndex.class,		MemberEditor.class,			true),
	SPOUSE		("spouse",		"Spouse",		MemberIndex.class,		MemberEditor.class,			true),
	ADDRESS		("address",		"Address",		AddressIndex.class,		null,						false);
	
	public final Field field;
	public final String displayName;
	public final Class<?> fieldClass;
	public final boolean editable;
	public final Class<?> editorClass;
	
	HouseholdField(String name, String displayName, Class<?> fieldClass, Class<?> editorClass, boolean editable) {
		try {
			this.field = Household.class.getDeclaredField(name);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("HouseholdField unknown field name " + name);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("HouseholdField unknown field name " + name);
		}
		this.displayName = displayName;
		this.fieldClass = fieldClass;
		this.editorClass = editorClass;
		this.editable = editable;
	}
}
