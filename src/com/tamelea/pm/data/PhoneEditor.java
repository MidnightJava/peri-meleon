package com.tamelea.pm.data;

import java.awt.Window;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

public class PhoneEditor extends FieldEditor {
	/*
	 * We override JFormattedTextField to prevent blank phone numbers
	 * being rendered as "() -". This is needed in particular for the
	 * entries in the Phone List.
	 */
	private JFormattedTextField textField;
	
	public PhoneEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		MaskFormatter formatter = null;
		try {
			formatter = new MaskFormatter("(***) ***-****");
			formatter.setValidCharacters(" 0123456789");
			textField = new JFormattedTextField(formatter);
			if (initial == null ||((Phone)initial).value.equals("")){
				textField.setText("");
				textField.commitEdit();
			}else {
				textField.setText(((Phone)initial).value);
				textField.commitEdit();
			}
		} catch (ParseException e) {
			throw new IllegalStateException("PhoneEditor: phone mask bad! Woe is us!", e);
		}
	}

	@Override
	public JComponent getComponent() {
		return textField;
	}

	@Override
	public Object getValue() {
		String text = ((String)textField.getValue());
		if (text.matches(".*\\d+.*")){
			return new Phone(text);
		}else{
			return new Phone("");
		}
	}
	
	@Override
	public boolean isValid() {
		return true;
	}
}
