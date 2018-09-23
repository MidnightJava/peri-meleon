package com.tamelea.pm.data;

import java.util.Comparator;

final class DisplayNameComparator implements Comparator<MemberIndex> {
	private Data data;
	
	DisplayNameComparator(Data data) {
		this.data = data;
	}

	public int compare(MemberIndex o1, MemberIndex o2) {
		String name1 = data.getMemberDisplayName(o1);
		String name2 = data.getMemberDisplayName(o2);
		return name1.compareTo(name2);
	}
}
