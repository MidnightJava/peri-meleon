/**
 * Immutable class to represent member index.
 * Suitable for use as key.
 */
package com.tamelea.pm.data;

public final class MemberIndex implements Comparable<MemberIndex>, IntegerIndex {
	final int value;
	
	MemberIndex(int value) {
		this.value = value;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof MemberIndex)) return false;
		return value == ((MemberIndex)o).value;
	}
	
	public int hashCode() {
		return value;
	}
	
	public int compareTo(MemberIndex i) {
		if (i.value < value) return -1;
		else if (i.value > value) return 1;
		else return 0;
	}
	
	public String toString() {
		return Integer.toString(value);
	}
	
	public static MemberIndex valueOf(String string) {
		if (string.equals("")) return null;
		return new MemberIndex(Integer.parseInt(string));
	}

	@Override
	public int value() {
		return value;
	}
}
