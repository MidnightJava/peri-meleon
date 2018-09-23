package com.tamelea.pm.data;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 * For optional dates.
 * @author FKUHL
 *
 */
public final class DateEditor extends FieldEditor {
	private MaskFormatter formatter; 
	private JFormattedTextField textField;
	private boolean valid;
	public DateEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		formatter = null;
		try {
			formatter = new MaskFormatter("**/**/****");
			formatter.setValidCharacters(" 0123456789");
			textField = new JFormattedTextField(new DefaultFormatterFactory(formatter));
			if (initial == null || ((PMDate)initial).getValue() == null){
				textField.setText("");
				textField.commitEdit();
			} else{
				textField.setText(((PMDate)initial).toString());
				textField.commitEdit();
			}
		} catch (java.text.ParseException e) {
			throw new IllegalStateException("DateEditor: date mask bad! Woe is us!", e);
		}
	}

	@Override
	public JComponent getComponent() {
		return textField;
	}

	@Override
	public Object getValue() {
		try {
			String parseString = textField.getText();
			if (parseString.equals("  /  /    ")) {
				valid = true;
				return null;
			}
			PMDate result = new PMDate(parseString);
			valid = true;
			return result;
		} catch (java.text.ParseException e) {
			valid = false;
			return null;
		}
	}
	
	@Override
	public boolean isValid() {
		getValue();
		return valid;
	}

}
