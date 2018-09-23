package com.tamelea.pm.data;

import java.util.Comparator;

public final class HouseholdNameComparator implements Comparator<HouseholdIndex> {
	private Data data;
	
	public HouseholdNameComparator(Data data) {
		this.data = data;
	}

	public int compare(HouseholdIndex o1, HouseholdIndex o2) {
		String name1 = ((PMString)data.getHouseholdValue(o1, HouseholdField.NAME)).toString();
		String name2 = ((PMString)data.getHouseholdValue(o2, HouseholdField.NAME)).toString();
		return name1.compareTo(name2);
	}
}
