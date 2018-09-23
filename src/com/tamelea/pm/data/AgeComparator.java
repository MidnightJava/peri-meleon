package com.tamelea.pm.data;

import java.util.Comparator;

final class AgeComparator implements Comparator<MemberIndex> {
	private Data data;
	
	AgeComparator(Data data) {
		this.data = data;
	}

	public int compare(MemberIndex o1, MemberIndex o2) {
		PMDate dob1 = (PMDate)data.getMemberValue(o1, MemberField.DATE_OF_BIRTH);
		PMDate dob2 = (PMDate)data.getMemberValue(o2, MemberField.DATE_OF_BIRTH);
		if (dob1 == null)return -1;
		if (dob2 == null)return 1;
		return dob1.compareTo(dob2);
	}
}
