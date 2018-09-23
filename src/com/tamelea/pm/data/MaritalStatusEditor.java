package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public final class MaritalStatusEditor extends FieldEditor {
	private JComboBox box;

	public MaritalStatusEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		box = new JComboBox(MaritalStatus.values());
		if (initial != null) box.setSelectedItem((MaritalStatus)initial);
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
