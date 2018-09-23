package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.FieldEditor;
import com.tamelea.pm.data.HouseholdCandidatesTableModel;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.PMString;

@SuppressWarnings("serial")
final class EditHouseholdDialog extends JDialog {
	private View								view;
	private Data								data;
	private HouseholdIndex						household;
	private AddressIndex						address;
	private JPanel								insetPane;
	private EnumMap<HouseholdField, FieldEditor>editors;
	private EnumMap<AddressField, FieldEditor>	addressEditors;
	private JButton								closeButton;
	private JButton								cancelButton;
	private JButton								getMapButton;
	
	EditHouseholdDialog(View view, Data data, HouseholdIndex household) {
		super(view, "Edit Household", true);
		this.view = view;
		this.data = data;
		this.household = household;
		insetPane = new JPanel();
		insetPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		insetPane.setLayout(new SpringLayout());
		editors = new EnumMap<HouseholdField, FieldEditor>(HouseholdField.class);
		for (HouseholdField field : HouseholdField.values()) {
			if (field.editable) {
				insetPane.add(new JLabel(field.displayName));
				FieldEditor editor = null;
				Object storedValue = data.getHouseholdValue(household, field);
				try {
					Constructor<?> ctor = field.editorClass.getDeclaredConstructor(
							Window.class, Data.class, Object.class);
					editor = (FieldEditor) ctor.newInstance(this, data, storedValue);
				} catch (Exception e) {
					throw new IllegalStateException(
							"EditHouseholdDialog ctor: can't instantiate editor for "
									+ field, e);
				}
				editors.put(field, editor);
				insetPane.add(editor.getComponent());
			}			
		}
		address = (AddressIndex)data.getHouseholdValue(household, HouseholdField.ADDRESS);
		addressEditors = new EnumMap<AddressField, FieldEditor>(AddressField.class);
		for (AddressField field : AddressField.values()) {
			if (field.editable) {
				insetPane.add(new JLabel(field.displayName));
				FieldEditor editor = null;
				Object storedValue = data.getAddressValue(address, field);
				try {
					Constructor<?> ctor = field.editorClass.getDeclaredConstructor(
							Window.class, Data.class, Object.class);
					editor = (FieldEditor) ctor.newInstance(this, data, storedValue);
				} catch (Exception e) {
					throw new IllegalStateException(
							"EditHouseholdDialog ctor: can't instantiate editor for "
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
		
		JTable otherTable = new JTable(new HouseholdCandidatesTableModel(data, household));
		otherTable.getColumnModel().getColumn(0).setMaxWidth(20);
		JScrollPane otherJsp = new JScrollPane(otherTable);
		otherJsp.setPreferredSize(new Dimension(200, 200));
		JPanel otherPanel = new JPanel(new BorderLayout());
		otherPanel.setBorder(BorderFactory.createTitledBorder("Other Household Members"));
		otherPanel.add(otherJsp, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		getMapButton = new JButton("Get Map to Address");
		getMapButton.addActionListener(new GetMapButtonListener());
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(getMapButton);
		southPanel.add(Box.createHorizontalGlue());
		closeButton = new JButton("Save Edits and Close");
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
		overall.add(otherPanel);
		overall.add(Box.createVerticalStrut(5));
		overall.add(southPanel);
		overall.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, 
				overall.getBackground()));
		this.add(overall, BorderLayout.CENTER);
		
		PMString name = (PMString)data.getHouseholdValue(household, HouseholdField.NAME);
		this.setTitle("Edit Household: " + name);
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
				EnumMap<HouseholdField, Object> values = 
					new EnumMap<HouseholdField, Object>(HouseholdField.class);
				for (HouseholdField field : editors.keySet()) {
					FieldEditor editor = editors.get(field);
					Object datum = editor.getValue();
					values.put(field, datum);
//					System.out.println(field.toString() + ": " + datum);
				}
				data.setHouseholdValues(household, values);
				for (AddressField field : addressEditors.keySet()) {
					FieldEditor editor = addressEditors.get(field);
					Object datum = editor.getValue();
					data.setAddressValue(address, field, datum);
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
	
	private final class GetMapButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			view.showMap();
		}
	}
}
