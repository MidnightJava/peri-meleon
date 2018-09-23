package com.tamelea.pm;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.tamelea.pm.data.MemberAgeFilter;
import com.tamelea.pm.data.ComparisonOperator;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.DateEditor;
import com.tamelea.pm.data.MembersByAgeTableModel;
import com.tamelea.pm.data.PMDate;

@SuppressWarnings("serial")
public class AgeQueryDialog extends JDialog
implements ActionListener{
	private PeriMeleonView	membersView;
	private DateEditor		dateEditor;
	private JTextField		ageField;
	private JComboBox		opChoices;
	private JComboBox		sortFieldChoices;
	private JCheckBox		activeOnly;
	private Data			data;
	private MemberAgeFilter	filter;
	
	public AgeQueryDialog(Data data, PeriMeleonView view){
		super(view, "Find members by age", true);
		this.membersView = view;
		this.data = data;
		dateEditor = new DateEditor(this, data, PMDate.getToday());
		JComponent dateField = dateEditor.getComponent();
		dateField.setPreferredSize(new Dimension(120,25));
		ageField = new JTextField(2);
		ageField.setText("5");
		ageField.setInputVerifier(new AgeVerifier());
		opChoices = new JComboBox();
		opChoices.setModel(new DefaultComboBoxModel(ComparisonOperator.values()));
		sortFieldChoices = new JComboBox();
		sortFieldChoices.setModel(new DefaultComboBoxModel(MembersByAgeTableModel.getSortFieldValues()));
		sortFieldChoices.removeItemAt(0);//remove empty column name for record number
		activeOnly = new JCheckBox("Find active members only");
		activeOnly.setSelected(true);
		JButton executeButton = new JButton("Execute Query");
		executeButton.setActionCommand("execute");
		executeButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane,BoxLayout.Y_AXIS));
		mainPane.add(Box.createVerticalStrut(10));
			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
			p1.add(new JLabel("Show all members whose age as of"));
			p1.add(Box.createHorizontalStrut(10));
			p1.add(Box.createHorizontalGlue());
		mainPane.add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
			p2.add(dateField);
			p2.add(Box.createHorizontalStrut(5));
			p2.add(new JLabel("is"));
			p2.add(Box.createHorizontalStrut(5));
			p2.add(opChoices);
			p2.add(Box.createHorizontalStrut(5));
			p2.add(ageField);
			p2.add(Box.createHorizontalStrut(5));
			p2.add(new JLabel("years"));
			p2.add(Box.createHorizontalStrut(10));
			p2.add(Box.createHorizontalGlue());
		mainPane.add(Box.createVerticalStrut(10));
		mainPane.add(p2);
			JPanel p3 = new JPanel();
			p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
			p3.add(new JLabel("Sort results by"));
			p3.add(Box.createHorizontalStrut(5));
			p3.add(sortFieldChoices);
			p3.add(Box.createHorizontalGlue());
		mainPane.add(Box.createVerticalStrut(10));
		mainPane.add(p3);
			JPanel p4 = new JPanel();
			p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS));
			p4.add(activeOnly);
			p4.add(Box.createHorizontalGlue());
			mainPane.add(p4);
			JPanel buttonPanel =new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
			buttonPanel.add(executeButton);
			buttonPanel.add(Box.createHorizontalStrut(10));
			buttonPanel.add(cancelButton);
			buttonPanel.add(Box.createHorizontalGlue());
		mainPane.add(Box.createVerticalStrut(10));
		mainPane.add(buttonPanel);
		mainPane.add(Box.createVerticalGlue());
		JPanel insetPane = new JPanel();
		insetPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		insetPane.add(mainPane);
		this.add(insetPane);
		pack();
		place();
	}
	
	private void place() {
		java.awt.Point viewCenter = membersView.getCenter();
		java.awt.Dimension size = this.getSize();
		setLocation(Math.max(0, viewCenter.x - size.width / 2), 
				Math.max(0, viewCenter.y - size.height / 2));
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("execute")){
			PMDate date = (PMDate)dateEditor.getValue();
			if (date == null) {
				JOptionPane.showMessageDialog(this, "Date is not valid.", "Date Is Not Valid", JOptionPane.ERROR_MESSAGE);
				dispose();
				return;
			}
			ComparisonOperator op = (ComparisonOperator)opChoices.getSelectedItem();
			if (!ageField.getInputVerifier().verify(ageField)) {
				JOptionPane.showMessageDialog(this, "Age is not valid.", "Age Is Not Valid", JOptionPane.ERROR_MESSAGE);
				dispose();
				return;
			}
			double fractionalAge = Double.parseDouble(ageField.getText());
			int age =  (int)Math.ceil(fractionalAge);
			filter = new MemberAgeFilter(data, date, op, age, activeOnly.isSelected());
			String sortFieldName = (String)sortFieldChoices.getSelectedItem();
			dispose();
			membersView.launchView(MembersByAgeView.class, data, membersView, sortFieldName, filter);
		} else if (e.getActionCommand().equals("cancel")){
			dispose();
		}
	}
	
	class AgeVerifier extends InputVerifier {
		public boolean verify(JComponent input) {
			boolean result = false;
			JTextField tf = (JTextField)input;
			try {
				Double age = Double.parseDouble(tf.getText());
				if (age >= 0.0 && age < 150.0) return true;
			} catch (NumberFormatException e) {
				return false;
			}
			return result;
		}
	}

}
