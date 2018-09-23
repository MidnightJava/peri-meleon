package com.tamelea.pm.data;

/**
 * Holds a telephone number.
 * The point of having a distinct class is to signal input UIs
 * that the field should be formatted.
 * Immutable!
 *
 */
public final class Phone implements Comparable<Phone> {
	public final String value;
	
	public Phone(String value) {
		this.value = value;
	}
	
	public String toString() {
		if (value == null) return "";
		return value;
	}
	
	public static Phone valueOf(String text) {
		return new Phone(text);
	}
	
	public int compareTo(Phone i) {
		return value.compareTo(i.value); 
	}
}
