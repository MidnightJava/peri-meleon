package com.tamelea.pm;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.Month;
import com.tamelea.pm.data.MemberStatus;
import com.tamelea.pm.data.ResidenceSelector;
/**
 * The File menu.
 *
 */
@SuppressWarnings("serial")
final class QueriesEdit {
	private PeriMeleonView				view;
	private Data						data;
	private JMenu						queriesMenu;
	private QueriesBirthdaysAction		queriesBirthdays;
	private QueriesMemberStatusAction	queriesMemberStatus;
	private QueriesMemberAgeAction		queriesMemberAge;
	private QueriesStatisticalAction	queriesStatistical;
	private QueriesBaptismsAction		queriesBaptisms;
	private ActiveHouseholdsAction		activeHouseholds;
	private JCheckBox 					resident;
	private JCheckBox 					nonResident;
	
	QueriesEdit(PeriMeleonView view, Data data) {
		this.view = view;
		this.data = data;
	}
	
	public JMenu createMenu() {
		queriesMenu = new JMenu("Queries");
		
		queriesBirthdays = new QueriesBirthdaysAction();
		queriesMemberStatus = new QueriesMemberStatusAction();
		queriesMemberAge = new QueriesMemberAgeAction();
		queriesStatistical = new QueriesStatisticalAction();
		queriesBaptisms = new QueriesBaptismsAction();
		activeHouseholds = new ActiveHouseholdsAction();
		queriesMenu.add(queriesBirthdays);
		queriesMenu.add(queriesMemberStatus);
		queriesMenu.add(queriesMemberAge);
		queriesMenu.add(queriesStatistical);
		queriesMenu.add(queriesBaptisms);
		queriesMenu.add(activeHouseholds);
		return queriesMenu;
	}
	
	public void setEnabled(boolean value) {
		queriesBirthdays.setEnabled(value);
		queriesMemberStatus.setEnabled(value);
		queriesMemberAge.setEnabled(value);
		queriesStatistical.setEnabled(value);
		queriesBaptisms.setEnabled(value);
		activeHouseholds.setEnabled(value);
	}
	
	private final class QueriesBirthdaysAction extends AbstractAction {

	    public QueriesBirthdaysAction() {
			super("Birthdays...");
		}
		
		public void actionPerformed(ActionEvent e) {
			Month chosen = (Month)JOptionPane.showInputDialog(
					view,
					"Show active members with birthdays in:",
					"Birthdays",
					JOptionPane.PLAIN_MESSAGE,
					null,
					Month.values(),
					Month.JANUARY);
			if (chosen != null) {
				new BirthdaysView(data, view, chosen).sizeAndPlace();
			}
		}
	}
	
	private final class ActiveHouseholdsAction extends AbstractAction {

	    public ActiveHouseholdsAction() {
			super("Active households...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new ActiveHouseholdsView(data, view).sizeAndPlace();
		}
	}
	
	private final class QueriesMemberStatusAction extends AbstractAction {

	    public QueriesMemberStatusAction() {
			super("Search Members by Status...");
		}
		
		public void actionPerformed(ActionEvent e) {
			ResidenceSelector rs;
			resident = new JCheckBox("Resident");
			resident.setActionCommand("resident");
			resident.setSelected(true);
			resident.addActionListener(new CheckBoxListener());
			nonResident = new JCheckBox("Non Resident");
			nonResident.setSelected(false);
			nonResident.addActionListener(new CheckBoxListener());
			JPanel mainPane = new JPanel();
			mainPane.setLayout(new GridLayout(2, 3, 2, 2));
			JPanel pane = new JPanel();
			String msg = "Show members with the following status:";
			pane.add(new JLabel(msg));
			pane.add(resident);
			pane.add(nonResident);
			mainPane.add(pane);
			pane = new JPanel();
			msg = "Name contains:";
			pane.add(new JLabel(msg));
			JTextField text = new JTextField();
			text.setColumns(30);
			pane.add(text);
			mainPane.add(pane);
			MemberStatus chosen = (MemberStatus)JOptionPane.showInputDialog(
					view,
					mainPane,
					"Members by Status",
					JOptionPane.PLAIN_MESSAGE,
					null,
					MemberStatus.values(),
					MemberStatus.NONCOMMUNING);
			if (chosen != null) {
				if (resident.isSelected() && nonResident.isSelected()){
					rs = ResidenceSelector.BOTH;
				} else{
					if (resident.isSelected()){
						rs = ResidenceSelector.RESIDENTS;
					} else{
						rs = ResidenceSelector.NON_RESIDENTS;
					}
				}
				new MembersByStatusView(data, view, chosen, rs, text.getText()).sizeAndPlace();
			}
		}
		
		private final class CheckBoxListener implements ActionListener{

			public void actionPerformed(ActionEvent e) {
				if (!resident.isSelected() && !nonResident.isSelected()){
					if (e.getActionCommand().equals("resident")){
						nonResident.setSelected(true);
					} else{
						resident.setSelected(true);
					}
				}
			}
		}
	}
	
	private final class QueriesMemberAgeAction extends AbstractAction {

	    public QueriesMemberAgeAction() {
			super("Members by Age...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new AgeQueryDialog(data, view).setVisible(true);
		}
	}
	
	private final class QueriesStatisticalAction extends AbstractAction {

	    public QueriesStatisticalAction() {
			super("Transactions for statistics...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new StatisticalQueryDialog(data, view).setVisible(true);
		}
	}
	
	private final class QueriesBaptismsAction extends AbstractAction {

	    public QueriesBaptismsAction() {
			super("Baptisms...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new BaptismsQueryDialog(data, view).setVisible(true);
		}
	}
}
