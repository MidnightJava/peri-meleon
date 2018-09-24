package com.tamelea.pm.data;

final class MemberStatusFilter implements MemberFilter {
	private Data data;
	private MemberStatus statusMatch;
	private ResidenceSelector rs;
	private String nameSearch;
	
	MemberStatusFilter(Data data, MemberStatus statusMatch, ResidenceSelector rs, String nameSearch) {
		this.data = data;
		this.statusMatch = statusMatch;
		this.rs = rs;
		this.nameSearch = nameSearch;
	}

	public boolean match(MemberIndex index) {
		boolean residenceMatch  = false;
		MemberStatus status = (MemberStatus)data.getMemberValue(index, MemberField.STATUS);
		PMString firstName = (PMString)data.getMemberValue(index, MemberField.FIRST_NAME);
		PMString middleName = (PMString)data.getMemberValue(index, MemberField.MIDDLE_NAME);
		PMString lastName = (PMString)data.getMemberValue(index, MemberField.LAST_NAME);
		PMString nickName = (PMString)data.getMemberValue(index, MemberField.NICK_NAME);
		Boolean nameMatch = true;
		if (nameSearch.trim().length() > 0) {
			nameMatch = firstName.toString().toLowerCase().contains(nameSearch.toLowerCase()) ||
					middleName.toString().toLowerCase().contains(nameSearch.toLowerCase()) ||
					lastName.toString().toLowerCase().contains(nameSearch.toLowerCase()) ||
					nickName.toString().toLowerCase().contains(nameSearch.toLowerCase());
		}
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
		return statusMatch == status && nameMatch && residenceMatch;
	}

}
