package com.tamelea.pm.data;

final class ActiveMemberFilter implements MemberFilter {
	private Data data;
	
	ActiveMemberFilter(Data data) {
		this.data = data;
	}

	public boolean match(MemberIndex index) {
		return data.isActive(index);
	}

}
