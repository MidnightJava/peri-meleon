package com.tamelea.pm.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * A general comparator for MemberIndex that is parameterized by sort field.
 */
final class MemberFieldComparator implements Comparator<MemberIndex> {
	private Data data;
	private MemberField sortField;
	private Method comparator;
	
	MemberFieldComparator(Data data, MemberField sortField) {
		try {
			this.data = data;
			this.sortField = sortField;
			Class<?> fieldClass = sortField.fieldClass;
			//This hack is necessitated by the fact that enums have a final compareTo method
			//that takes an enum. Thanks, Sun.
			if (fieldClass.isEnum()) this.comparator = fieldClass.getMethod("compareTo", Enum.class);
			else this.comparator = fieldClass.getMethod("compareTo", fieldClass);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Implement Comparator by invoking compare on the sort field's datatype.
	 * Alas, it doesn't appear to be possible to do this without resorting to reflection,
	 * because (a) some of the field types can't be extended, e.g., Boolean, and
	 * (b) the signature needed on field type T is compareTo(T), and I see no way
	 * to express that with generics.
	 * So, goodbye to static typing here.
	 */
	public int compare(MemberIndex i1, MemberIndex i2) {
		try {
			Object f1 = data.getMemberValue(i1, sortField);
			Object f2 = data.getMemberValue(i2, sortField);
			return (Integer)comparator.invoke(f1, f2);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
