package com.tamelea.pm.data;

import java.awt.Window;

public final class FemaleMemberEditor extends MemberEditor {
	
	public FemaleMemberEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
	}

	@Override
	protected MemberFilter getSexFilter() {
		return new SexFilter(Sex.FEMALE, data);
	}
}

