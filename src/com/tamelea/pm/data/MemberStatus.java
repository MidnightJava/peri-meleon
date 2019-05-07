package com.tamelea.pm.data;

public enum MemberStatus {
	NONCOMMUNING,
	COMMUNING,
	ASSOCIATE,
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
		|| status == ASSOCIATE
		|| status == PASTOR
		|| status == SUSPENDED;
	}
}
