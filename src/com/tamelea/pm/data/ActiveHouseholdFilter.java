package com.tamelea.pm.data;

public final class ActiveHouseholdFilter implements HouseholdFilter {
	private Data data;
	
	public ActiveHouseholdFilter(Data data) {
		this.data = data;
	}

	public boolean match(HouseholdIndex index) {
		MemberIndex head = (MemberIndex)data.getHouseholdValue(index, HouseholdField.HEAD);
		return head != null && data.isActive(head);
	}

}
