package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public final class BooleanEditor extends FieldEditor {
	private JCheckBox box;
	
	public BooleanEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		box = new JCheckBox();
		if (initial != null) box.setSelected((Boolean)initial); 
	}

	@Override
	public JComponent getComponent() {
		return box;
	}

	@Override
	public Object getValue() {
		return box.isSelected(); //ooh! auto-boxing!
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

}
