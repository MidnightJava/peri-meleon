package com.tamelea.pm.data;

import java.util.Comparator;

final class DayOfMonthComparator implements Comparator<MemberIndex> {
	private Data data;
	
	DayOfMonthComparator(Data data) {
		this.data = data;
	}

	public int compare(MemberIndex o1, MemberIndex o2) {
		PMDate dob1 = (PMDate)data.getMemberValue(o1, MemberField.DATE_OF_BIRTH);
		PMDate dob2 = (PMDate)data.getMemberValue(o2, MemberField.DATE_OF_BIRTH);
		return new Integer(dob1.getDayOfMonth()).compareTo(dob2.getDayOfMonth());
	}
}
