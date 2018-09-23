package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.FieldEditor;

@SuppressWarnings("serial")
public final class EditAddressDialog extends JDialog {
	private JDialog								parent;
	private Data								data;
	private AddressIndex						address;
	private JPanel								insetPane;
	private EnumMap<AddressField, FieldEditor>	addressEditors;
	private JButton								closeButton;
	private JButton								removeButton;
	private JButton								cancelButton;
	
	public EditAddressDialog(JDialog parent, Data data, AddressIndex address) {
		super(parent, "Edit Address", true);
		this.parent = parent;
		this.data = data;
		this.address = address;
		insetPane = new JPanel();
		insetPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		insetPane.setLayout(new SpringLayout());
		addressEditors = new EnumMap<AddressField, FieldEditor>(AddressField.class);
		for (AddressField field : AddressField.values()) {
			if (field.editable) {
				insetPane.add(new JLabel(field.displayName));
				FieldEditor editor = null;
				try {
					Constructor<?> ctor = field.editorClass
						.getDeclaredConstructor(Window.class, Data.class, Object.class);
					if (address != null) {
						Object storedValue = data.getAddressValue(address, field);
						editor = (FieldEditor) ctor.newInstance(this, data, storedValue);
					} else {
						editor = (FieldEditor) ctor.newInstance(this, data, null);
					}
				} catch (Exception e) {
					throw new IllegalStateException(
							"EditAddressDialog ctor: can't instantiate editor for "
							+ field, e);
				}
				addressEditors.put(field, editor);
				insetPane.add(editor.getComponent());
			}			
		}
		SpringUtilities.makeCompactGrid(insetPane,
                addressEditors.size(), 2,
                2, 2,
                2, 2);
		JPanel insetFlowPanel = new JPanel();
		insetFlowPanel.setLayout(new BorderLayout());
		insetFlowPanel.add(insetPane, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		southPanel.add(Box.createHorizontalGlue());
		closeButton = new JButton("Save Edits and Close");
		closeButton.addActionListener(new CloseButtonListener());
		southPanel.add(closeButton);
		removeButton = new JButton("Remove Address");
		removeButton.addActionListener(new RemoveButtonListener());
		southPanel.add(removeButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonListener());
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(cancelButton);
		southPanel.add(Box.createHorizontalGlue());

		JPanel overall = new JPanel();
		overall.setLayout(new BorderLayout());
		overall.add(insetFlowPanel, BorderLayout.CENTER);
		overall.add(southPanel, BorderLayout.SOUTH);
		overall.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, 
				overall.getBackground()));
		this.add(overall, BorderLayout.CENTER);
		pack();
		place();
	}
	
	private void place() {
		java.awt.Rectangle bounds = parent.getBounds();
		java.awt.Point viewCenter = new java.awt.Point(
				bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
		java.awt.Dimension size = this.getSize();
		setLocation(Math.max(0, viewCenter.x - size.width / 2), 
				Math.max(0, viewCenter.y - size.height / 2));
	}
	
	public AddressIndex getAddress() {
		return address;
	}
	
	private boolean inputsAreValid() {
		for (AddressField field : addressEditors.keySet()) {
			FieldEditor editor = addressEditors.get(field);
			if (!editor.isValid()) {
				JOptionPane.showMessageDialog(this, "Data for " + field.displayName + " missing or invalid", 
						"Invalid Data", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	private final class RemoveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			address = null;
			setVisible(false);
		}
	}

	private final class CloseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (inputsAreValid()) {
				if (address == null) address = data.addAddress();
				for (AddressField field : addressEditors.keySet()) {
					FieldEditor editor = addressEditors.get(field);
					Object datum = editor.getValue();
					data.setAddressValue(address, field, datum);
				}
				setVisible(false);
			} 
		}
	}

	private final class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}
}
