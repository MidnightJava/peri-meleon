package com.tamelea.pm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.MemberNameFilter;
import com.tamelea.pm.data.MembersByAgeTableModel;

@SuppressWarnings("serial")
public class NameQueryDialog extends JDialog
implements ActionListener{
	private PeriMeleonView		membersView;
	private JTextField			nameStringField;
	private JComboBox			sortFieldChoices;
	private JCheckBox			activeOnly;
	private Data				data;
	private MemberNameFilter	filter;
	
	public NameQueryDialog(Data data, PeriMeleonView view){
		super(view, "Find members by name", true);
		this.membersView = view;
		this.data = data;
		nameStringField = new JTextField();
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
			p1.add(new JLabel("Find members whose name contains:"));
			p1.add(Box.createHorizontalStrut(10));
			p1.add(Box.createHorizontalGlue());
		mainPane.add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
			p2.add(Box.createHorizontalStrut(5));
			p2.add(nameStringField);
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
			JPanel buttonPanel = new JPanel();
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
			String nameString = nameStringField.getText();
			filter = new MemberNameFilter(data, nameString, activeOnly.isSelected());
			String sortFieldName = (String)sortFieldChoices.getSelectedItem();
			dispose();
			membersView.launchView(MembersByNameView.class, data, membersView, sortFieldName, filter);
		} else if (e.getActionCommand().equals("cancel")){
			dispose();
		}
	}

}
