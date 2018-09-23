package com.tamelea.pm.data;

final class BirthdayMemberFilter implements MemberFilter {
	private Data data;
	private Month month;
	
	BirthdayMemberFilter(Data data, Month month) {
		this.data = data;
		this.month = month;
	}

	public boolean match(MemberIndex index) {
		boolean exDirectory = (Boolean)data.getMemberValue(index, MemberField.EX_DIRECTORY);
		if (data.isActive(index) && !exDirectory) {
			PMDate membersDOB = (PMDate)data.getMemberValue(index, MemberField.DATE_OF_BIRTH);
			if (membersDOB.isInMonth(month)) return true;
		}
		return false;
	}

}
