package com.tamelea.pm.data;

import java.awt.Window;

public final class MaleMemberEditor extends MemberEditor {
	
	public MaleMemberEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
	}

	@Override
	protected MemberFilter getSexFilter() {
		return new SexFilter(Sex.MALE, data);
	}
}