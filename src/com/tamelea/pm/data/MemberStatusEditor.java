package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public final class MemberStatusEditor extends FieldEditor {
	private JComboBox box;
	
	public MemberStatusEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		box = new JComboBox(MemberStatus.values());
		if (initial != null) box.setSelectedItem((MemberStatus)initial);
	}

	@Override
	public JComponent getComponent() {
		return box;
	}

	@Override
	public Object getValue() {
		return box.getSelectedItem();
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

}
