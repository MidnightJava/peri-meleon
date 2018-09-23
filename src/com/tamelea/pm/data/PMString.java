package com.tamelea.pm.data;

/**
 * Encapsulate a String and provide static valueOf().
 * Imutable, as usual.
 */
public final class PMString implements Comparable<PMString> {
	private final String string;
	
	public PMString(String string) {
		this.string = string;
	}
	
	public String toString() {
		return string;
	}
	
	public int length() {
		return string.length();
	}
	
	public static PMString valueOf(String string) {
		return new PMString(string);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PMString)) return false;
		PMString ms = (PMString)o;
		if (string == null) {
			return ms.string == null;
		} else {
			if (ms.string == null) return false;
			else return string.equals(ms.string);
		}
	}
	
	public int hashCode() {
		if (string == null) return 0;
		else return string.hashCode();
	}

	public int compareTo(PMString t) {
		if (string == null) {
			if (t.string == null) return 0;
			else return -1;
		} else {
			if (t.string == null) return 1;
			else return string.compareTo(t.string);
		}
	}
}
