package com.tamelea.pm.data;

final class MemberStatusFilter implements MemberFilter {
	private Data data;
	private MemberStatus statusMatch;
	private ResidenceSelector rs;
	
	MemberStatusFilter(Data data, MemberStatus statusMatch, ResidenceSelector rs) {
		this.data = data;
		this.statusMatch = statusMatch;
		this.rs = rs;
	}

	public boolean match(MemberIndex index) {
		boolean residenceMatch  = false;
		MemberStatus status = (MemberStatus)data.getMemberValue(index, MemberField.STATUS);
		Boolean residentFlag = (Boolean)data.getMemberValue(index, MemberField.RESIDENT);
		switch (rs) {
		case BOTH:
			residenceMatch = true;
			break;
		case NON_RESIDENTS:
			residenceMatch = !residentFlag;
			break;
		case RESIDENTS:
			residenceMatch = residentFlag;
			break;
		}
		return statusMatch == status && residenceMatch;
	}

}
