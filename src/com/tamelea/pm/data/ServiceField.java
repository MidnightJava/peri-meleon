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

public enum ServiceField implements DataField {
	INDEX	("index",	"Index",			MemberIndex.class,		null,						false),
	DATE	("date",	"Date (mm/dd/yyyy)",PMDate.class,		DateEditor.class,			true),
	TYPE	("type",	"Type",				ServiceType.class,		ServiceTypeEditor.class,	true),
	PLACE	("place",	"Place",			PMString.class,	StringEditor.class,			true),
	COMMENT	("comment",	"Comment",			PMString.class,	StringEditor.class,			true);
	
	public final Field field;
	public final String displayName;
	public final Class<?> fieldClass;
	public final Class<?> editorClass;
	public final boolean editable;
	
	ServiceField(String name, String displayName, Class<?> fieldClass, Class<?> editorClass, boolean editable) {
		try {
			this.field = Service.class.getDeclaredField(name);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("ServiceField unknown field name " + name);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("ServiceField unknown field name " + name);
		}
		this.displayName = displayName;
		this.fieldClass = fieldClass;
		this.editorClass = editorClass;
		this.editable = editable;
	}
}
