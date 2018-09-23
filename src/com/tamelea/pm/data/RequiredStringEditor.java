package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Same as StringEditor, but a empty String is invalid.
 *
 */
public final class RequiredStringEditor extends FieldEditor {
	private JTextField textField;
	
	public RequiredStringEditor(Window parent, Data data, Object initial) {
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
		return textField.getText().length() > 0;
	}

}
