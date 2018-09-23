package com.tamelea.pm.data;

import com.tamelea.pm.EditAddressDialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

public final class AddressEditor extends FieldEditor {
	private JButton button;
	private AddressIndex address;

	public AddressEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		this.address = (AddressIndex)initial;
		this.data = data;
		button = new JButton();
		setLabel();
		button.addActionListener(new ButtonListener());
	}
	
	private void setLabel() {
		String label = "[none]";
		if (address != null) {
			label = ((PMString)data.getAddressValue(address, AddressField.ADDRESS)).toString();
			String address2 = ((PMString)data.getAddressValue(address, AddressField.ADDRESS_2)).toString();
			if (address2.length() > 0) label += " " + address2;
			label += " " + ((PMString)data.getAddressValue(address, AddressField.CITY)).toString();
			label += ", " + ((PMString)data.getAddressValue(address, AddressField.STATE)).toString();
			label += " " + ((PMString)data.getAddressValue(address, AddressField.POSTAL_CODE)).toString();
			String country = ((PMString)data.getAddressValue(address, AddressField.COUNTRY)).toString();
			if (country.length() > 0) label += " " + country;
			if (label.length() > 30) {
				label = label.substring(0, 27) + "...";
			}
		}
		button.setText(label);
	}
	
	@Override
	public JComponent getComponent() {
		return button;
	}

	@Override
	public Object getValue() {
		return address;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	private final class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			EditAddressDialog dialog = new EditAddressDialog((JDialog)parent, data, address);
			dialog.setVisible(true);
			address = dialog.getAddress();
			setLabel();
		}
	}
}
