package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.PMDate;
import com.tamelea.pm.data.RequiredDateEditor;

@SuppressWarnings("serial")
public class BaptismsQueryDialog extends JDialog
	implements ActionListener
{
	private PeriMeleonView	membersView;
	RequiredDateEditor		earliestEditor;
	RequiredDateEditor		latestEditor;
	private Data			data;
	
	public BaptismsQueryDialog(Data data, PeriMeleonView view) {
		super(view, "Baptisms", true);
		this.membersView = view;
		this.data = data;
		earliestEditor = new RequiredDateEditor(view, data, getAYearAgo());
		latestEditor = new RequiredDateEditor(view, data, getToday());
		JButton executeButton = new JButton("Execute Query");
		executeButton.setActionCommand("execute");
		executeButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.add(new JLabel("Baptisms between these inclusive dates:"), BorderLayout.NORTH);
		JPanel fieldsPane = new JPanel(new GridLayout(2,2));
		fieldsPane.add(new JLabel("Earliest date:"));
		fieldsPane.add(earliestEditor.getComponent());
		fieldsPane.add(new JLabel("Latest date:"));
		fieldsPane.add(latestEditor.getComponent());
		mainPane.add(fieldsPane, BorderLayout.CENTER);
		JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPane.add(executeButton);
		buttonsPane.add(cancelButton);
		mainPane.add(buttonsPane, BorderLayout.SOUTH);
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
			PMDate earliest = (PMDate)earliestEditor.getValue();
			PMDate latest = (PMDate)latestEditor.getValue();
			if (earliest == null || latest == null) return;
			if (earliest.compareTo(latest) > 0) {
				JOptionPane.showMessageDialog(
						BaptismsQueryDialog.this, 
						"Earliest date must be before or equal to latest.", 
						"Dates In Order", 
						JOptionPane.ERROR_MESSAGE);
				setVisible(false);
				return;
			}
			setVisible(false);
			membersView.launchView(BaptismsView.class, data, membersView, earliest, latest);
		} else if (e.getActionCommand().equals("cancel")){
			setVisible(false);
		}
	}
	
	private PMDate getToday() {
		GregorianCalendar todayRaw = new GregorianCalendar();
		todayRaw.set(Calendar.HOUR, 0);
		todayRaw.set(Calendar.MINUTE, 0);
		todayRaw.set(Calendar.SECOND, 0);
		todayRaw.set(Calendar.MILLISECOND, 0);
		return new PMDate(todayRaw.getTime());
	}
	
	private PMDate getAYearAgo() {
		GregorianCalendar todayRaw = new GregorianCalendar();
		todayRaw.set(Calendar.HOUR, 0);
		todayRaw.set(Calendar.MINUTE, 0);
		todayRaw.set(Calendar.SECOND, 0);
		todayRaw.set(Calendar.MILLISECOND, 0);
		todayRaw.add(Calendar.YEAR, -1);
		return new PMDate(todayRaw.getTime());
	}

}
