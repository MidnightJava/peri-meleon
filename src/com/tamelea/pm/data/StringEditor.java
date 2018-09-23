package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Editor for Strings that are allowed to be empty (zero length)
 *
 */
public final class StringEditor extends FieldEditor {
	private JTextField textField;
	
	public StringEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		textField = new JTextField();
		if (initial != null) textField.setText(((PMString)initial).toString());
	}
	
	@Override
	public JComponent getComponent() {
		return textField;
	}

	@Override
	public Object getValue() {
		return new PMString(textField.getText());
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

}
