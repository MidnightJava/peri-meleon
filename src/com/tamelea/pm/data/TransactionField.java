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

public enum TransactionField implements DataField {
	INDEX				("index",			"Index",				MemberIndex.class,		null,						false),
	DATE				("date",			"Date (mm/dd/yyyy)",	PMDate.class,		DateEditor.class,			true),
	TYPE				("type",			"Type",					TransactionType.class,	TransactionTypeEditor.class,true),
	AUTHORITY			("authority", 		"Authority (BCO ref)",	PMString.class,	StringEditor.class,			true),
	CHURCH				("church",			"Church From or To",	PMString.class,	StringEditor.class,			true),
	COMMENT				("comment",			"Comment",				PMString.class,	StringEditor.class,			true);
	
	public final Field field;
	public final String displayName;
	public final Class<?> fieldClass;
	public final Class<?> editorClass;
	public final boolean editable;
	
	TransactionField(String name, String displayName, Class<?> fieldClass, Class<?> editorClass, boolean editable) {
		try {
			this.field = Transaction.class.getDeclaredField(name);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("TransactionField unknown field name " + name);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("TransactionField unknown field name " + name);
		}
		this.displayName = displayName;
		this.fieldClass = fieldClass;
		this.editorClass = editorClass;
		this.editable = editable;
	}
}
