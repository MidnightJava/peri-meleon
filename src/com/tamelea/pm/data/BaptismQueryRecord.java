package com.tamelea.pm.data;

/**
 * A record constructed to hold an answer from a baptism query.
 */
public final class BaptismQueryRecord implements Comparable<BaptismQueryRecord> {
	public final PMDate date;
	public final String displayName;
	
	public BaptismQueryRecord(PMDate date, String displayName) {
		this.date = date;
		this.displayName = displayName;
	}

	public int compareTo(BaptismQueryRecord arg0) {
		return date.compareTo(arg0.date);
	}
}
