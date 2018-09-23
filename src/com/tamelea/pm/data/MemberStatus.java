package com.tamelea.pm.data;

public enum MemberStatus {
	NONCOMMUNING,
	COMMUNING,
	EXCOMMUNICATED,
	SUSPENDED,
	DISMISSAL_PENDING,
	DISMISSED,
	REMOVED,
	DEAD,
	PASTOR,
	VISITOR; //seems sketchy
	
	public static boolean isActive(MemberStatus status) {
		return status == NONCOMMUNING
		|| status == COMMUNING
		|| status == PASTOR
		|| status == SUSPENDED;
	}
}
