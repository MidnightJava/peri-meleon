/**
 * Immutable class to represent address index.
 * Suitable for use as key.
 */
package com.tamelea.pm.data;

public final class AddressIndex implements Comparable<AddressIndex>, IntegerIndex {
	final int value;
	
	AddressIndex(int value) {
		this.value = value;
	}
	
	public static boolean equals(AddressIndex ha, AddressIndex hb) {
		if (ha != null) {
			if (hb != null) return ha.equals(hb);
			else return false;
		} else {
			if (hb == null) return true;
			else return false;
		}
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof AddressIndex)) return false;
		return value == ((AddressIndex)o).value;
	}
	
	public int hashCode() {
		return value;
	}
	
	public int compareTo(AddressIndex i) {
		if (i.value < value) return -1;
		else if (i.value > value) return 1;
		else return 0;
	}
	
	public String toString() {
		return Integer.toString(value);
	}
	
	public static AddressIndex valueOf(String string) {
		if (string.equals("")) return null;
		return new AddressIndex(Integer.parseInt(string));
	}

	@Override
	public int value() {
		return value;
	}
}
