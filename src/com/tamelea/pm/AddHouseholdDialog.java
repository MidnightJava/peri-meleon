package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;

@SuppressWarnings("serial")
final class AddHouseholdDialog extends JDialog {
	private View								view;
	private Data								data;
	private JPanel								insetPane;
	private EnumMap<HouseholdField, FieldEditor>editors;
	private EnumMap<AddressField, FieldEditor>	addressEditors;
	private JButton								closeButton;
	private JButton								cancelButton;
	
	AddHouseholdDialog(View view, Data data) {
		super(view, "Add Household", true);
		this.view = view;
		this.data = data;
		insetPane = new JPanel();
		insetPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		insetPane.setLayout(new SpringLayout());
		editors = new EnumMap<HouseholdField, FieldEditor>(HouseholdField.class);
		for (HouseholdField field : HouseholdField.values()) {
			if (field.editable) {
				insetPane.add(new JLabel(field.displayName));
				FieldEditor editor = null;
				try {
					Constructor<?> ctor = field.editorClass.getDeclaredConstructor(
							Window.class, Data.class, Object.class);
					editor = (FieldEditor) ctor.newInstance(this, data, null);
				} catch (Exception e) {
					throw new IllegalStateException(
							"AddHouseholdDialog ctor: can't instantiate editor for "
									+ field, e);
				}
				editors.put(field, editor);
				insetPane.add(editor.getComponent());
			}			
		}
		addressEditors = new EnumMap<AddressField, FieldEditor>(AddressField.class);
		for (AddressField field : AddressField.values()) {
			if (field.editable) {
				insetPane.add(new JLabel(field.displayName));
				FieldEditor editor = null;
				try {
					Constructor<?> ctor = field.editorClass.getDeclaredConstructor(
							Window.class, Data.class, Object.class);
					editor = (FieldEditor) ctor.newInstance(this, data, null);
				} catch (Exception e) {
					throw new IllegalStateException(
							"AddHouseholdDialog ctor: can't instantiate editor for "
									+ field, e);
				}
				addressEditors.put(field, editor);
				insetPane.add(editor.getComponent());
			}			
		}
		SpringUtilities.makeCompactGrid(insetPane,
                editors.size() + addressEditors.size(), 2,
                2, 2,
                2, 2);
		JPanel insetFlowPanel = new JPanel();
		insetFlowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		insetFlowPanel.add(insetPane);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		southPanel.add(Box.createHorizontalGlue());
		closeButton = new JButton("Add Data and Close");
		closeButton.addActionListener(new CloseButtonListener());
		southPanel.add(closeButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonListener());
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(cancelButton);
		southPanel.add(Box.createHorizontalGlue());

		JPanel overall = new JPanel();
		overall.setLayout(new BoxLayout(overall, BoxLayout.Y_AXIS));
		overall.add(insetFlowPanel);
		overall.add(Box.createVerticalStrut(5));
		overall.add(southPanel);
		overall.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, 
				overall.getBackground()));
		this.add(overall, BorderLayout.CENTER);
		pack();
		place();
	}
	
	private void place() {
		java.awt.Point viewCenter = view.getCenter();
		java.awt.Dimension size = this.getSize();
		setLocation(Math.max(0, viewCenter.x - size.width / 2), 
				Math.max(0, viewCenter.y - size.height / 2));
	}
	
	private boolean inputsAreValid() {
		for (HouseholdField field : editors.keySet()) {
			FieldEditor editor = editors.get(field);
			if (!editor.isValid()) {
				JOptionPane.showMessageDialog(this, "Data for " + field.displayName + " missing or invalid", 
						"Invalid Data", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
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

	private final class CloseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (inputsAreValid()) {
				HouseholdIndex householdIndex = data.addHousehold();
				EnumMap<HouseholdField, Object> values = 
					new EnumMap<HouseholdField, Object>(HouseholdField.class);
				for (HouseholdField field : editors.keySet()) {
					FieldEditor editor = editors.get(field);
					Object datum = editor.getValue();
					values.put(field, datum);
				}
				//indices aren't editable, so weren't included above
				values.put(HouseholdField.INDEX, householdIndex);
				AddressIndex addressIndex = data.addAddress();
				values.put(HouseholdField.ADDRESS, addressIndex);
				data.setHouseholdValues(householdIndex, values);
				for (AddressField field : addressEditors.keySet()) {
					FieldEditor editor = addressEditors.get(field);
					Object datum = editor.getValue();
					data.setAddressValue(addressIndex, field, datum);
//					System.out.println(field.toString() + ": " + datum);
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
