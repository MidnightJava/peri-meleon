/**
 * Immutable class to represent household index.
 * Suitable for use as key.
 */
package com.tamelea.pm.data;

public final class HouseholdIndex implements Comparable<HouseholdIndex> {
	final int value;
	
	HouseholdIndex(int value) {
		this.value = value;
	}
	
	public static boolean equals(HouseholdIndex ha, HouseholdIndex hb) {
		if (ha != null) {
			if (hb != null) return ha.equals(hb);
			else return false;
		} else {
			if (hb == null) return true;
			else return false;
		}
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof HouseholdIndex)) return false;
		return value == ((HouseholdIndex)o).value;
	}
	
	public int hashCode() {
		return value;
	}
	
	public int compareTo(HouseholdIndex i) {
		if (i.value < value) return -1;
		else if (i.value > value) return 1;
		else return 0;
	}
	
	public String toString() {
		return Integer.toString(value);
	}
	
	public static HouseholdIndex valueOf(String string) {
		if (string.equals("")) return null;
		return new HouseholdIndex(Integer.parseInt(string));
	}
}
