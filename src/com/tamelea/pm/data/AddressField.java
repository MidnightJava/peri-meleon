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
public enum AddressField implements DataField {
	INDEX		("index",		"Index",		AddressIndex.class,	null,						false),
	ADDRESS		("address",		"Address",		PMString.class,		RequiredStringEditor.class,	true),
	ADDRESS_2	("address2",	"Address 2",	PMString.class,		StringEditor.class,			true),
	CITY		("city",		"City",			PMString.class,		RequiredStringEditor.class,	true),
	STATE		("state",		"State",		PMString.class,		RequiredStringEditor.class,	true),
	POSTAL_CODE	("postalCode",	"Postal Code",	PMString.class,		RequiredStringEditor.class,	true),
	COUNTRY		("country",		"Country",		PMString.class,		StringEditor.class,			true),
	EMAIL		("eMail",		"E-mail",		PMString.class,		StringEditor.class,			true),
	HOME_PHONE	("homePhone",	"Home Phone",	Phone.class,		PhoneEditor.class,			true);
	
	public final Field field;
	public final String displayName;
	public final Class<?> fieldClass;
	public final boolean editable;
	public final Class<?> editorClass;
	
	AddressField(String name, String displayName, Class<?> fieldClass, Class<?> editorClass, boolean editable) {
		try {
			this.field = Address.class.getDeclaredField(name);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("AddressField unknown field name " + name);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("AddressField unknown field name " + name);
		}
		this.displayName = displayName;
		this.fieldClass = fieldClass;
		this.editorClass = editorClass;
		this.editable = editable;
	}
}
