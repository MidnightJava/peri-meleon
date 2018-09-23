package com.tamelea.pm.data;

import java.util.Date;

final class OlderActiveMemberFilter implements MemberFilter {
	private Data data;
	private Date earliestDOB;
	
	OlderActiveMemberFilter(Data data, int minimumAge) {
		this.data = data;
		this.earliestDOB = Data.getEarliestDOB(minimumAge);
	}

	public boolean match(MemberIndex index) {
		if (data.isActive(index)) {
			PMDate dob = (PMDate)data.getMemberValue(index, MemberField.DATE_OF_BIRTH);
			//hack to include members without DOB recorded
			if (dob.isEmpty() || dob.before(earliestDOB)) return true;
		}
		return false;
	}

}
