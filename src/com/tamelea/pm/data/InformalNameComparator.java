package com.tamelea.pm.data;

import java.util.Comparator;

final class InformalNameComparator implements Comparator<MemberIndex> {
	private Data data;
	
	InformalNameComparator(Data data) {
		this.data = data;
	}

	public int compare(MemberIndex o1, MemberIndex o2) {
		String name1 = data.makeInformalName(o1);
		String name2 = data.makeInformalName(o2);
		return name1.compareTo(name2);
	}
}
