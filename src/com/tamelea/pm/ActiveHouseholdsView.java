package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.tamelea.pm.data.ActiveHouseholdsTableModel;
import com.tamelea.pm.data.ActiveSelector;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;

@SuppressWarnings("serial")
public final class ActiveHouseholdsView extends View {
	private ActiveHouseholdsTableModel	tableModel;
	private PMTable						table;
	private PeriMeleonView				membersView;
	private JMenuBar					menuBar;
	private HouseholdsGetMapAction 		householdsGetMap;
	
	ActiveHouseholdsView(Data data, PeriMeleonView membersView) {
		super("Active Households");
		this.data = data;
		this.membersView = membersView;
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
        com.tamelea.pm.PeriMeleonView.class.getResource("icon16.gif")));
		tableModel = new ActiveHouseholdsTableModel(data);
		table = new PMTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		JScrollPane jsp = new JScrollPane(table);
		this.getContentPane().add(jsp, BorderLayout.CENTER);
		addMenuBar();
		table.addMouseListener(new HouseholdsMouseListener(this));
	}
	
	private void addMenuBar() {
		JMenu householdsMenu = new JMenu("Households");
		householdsMenu.addMenuListener(new HouseholdsMenuListener());
		householdsMenu.add(new HouseholdsNewAction());
		householdsMenu.add(new HouseholdsEditAction());
		householdsMenu.add(new HouseholdsRemoveAction());
		householdsMenu.add(new HouseholdsExportAction(ActiveSelector.ACTIVE));
		householdsGetMap = new HouseholdsGetMapAction();
		householdsMenu.add(householdsGetMap);
		menuBar = new JMenuBar();
		menuBar.add(householdsMenu);
		this.setJMenuBar(menuBar);
	}
	
	void sizeAndPlace() {
		setVisible(true);
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int screenWidth = (screenSize.width > 1024) ? (int)(screenSize.width * 0.9)
	    		: screenSize.width;
	    setSize(screenWidth, screenSize.height / 2);
	    validate();
	    setLocation(screenSize.width - getSize().width, (screenSize.height - getSize().height) / 2);
	    toFront();
	}

	public void editHousehold() {
		int tableRowIndex = table.getSelectedRow();
		if (tableRowIndex < 0) return;
		new EditHouseholdDialog(this, data, tableModel.getHouseholdIndex(tableRowIndex)).setVisible(true);
	}
	
	private final class HouseholdsNewAction extends AbstractAction {

	    public HouseholdsNewAction() {
			super("New household...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new AddHouseholdDialog(ActiveHouseholdsView.this, data).setVisible(true);
		}
	}
	
	private final class HouseholdsEditAction extends AbstractAction {

	    public HouseholdsEditAction() {
			super("Edit household...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = table.getSelectedRow();
			if (tableIndex < 0) return;
			HouseholdIndex householdIndex = tableModel.getHouseholdIndex(tableIndex);
			new EditHouseholdDialog(ActiveHouseholdsView.this, data, householdIndex).setVisible(true);
		}
	}
	
	private final class HouseholdsRemoveAction extends AbstractAction {

	    public HouseholdsRemoveAction() {
			super("Remove household...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = table.getSelectedRow();
			if (tableIndex < 0) return;
			HouseholdIndex householdIndex = tableModel.getHouseholdIndex(tableIndex);
			membersView.removeHousehold(ActiveHouseholdsView.this, householdIndex);
		}
	}
	
	private final class HouseholdsGetMapAction extends AbstractAction {

	    public HouseholdsGetMapAction() {
			super("Show map to selected household address");
		}
		
		public void actionPerformed(ActionEvent e) {
			showMap();
		}
	}
	
	private final class HouseholdsMenuListener implements MenuListener {

		public void menuCanceled(MenuEvent e) { }

		public void menuDeselected(MenuEvent e) { }

		public void menuSelected(MenuEvent e) {
			if (getSelectedHouseholdIndex() == null){
				householdsGetMap.setEnabled(false);
			} else{
				householdsGetMap.setEnabled(true);
			}
		}
		
		public HouseholdIndex getSelectedHouseholdIndex(){
			int tableIndex = table.getSelectedRow();
			if (tableIndex < 0) return null;
			return tableModel.getHouseholdIndex(tableIndex);
		}
	}
	
	public void showMap(){
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return;
		HouseholdIndex householdIndex = tableModel.getHouseholdIndex(tableIndex);
		AddressIndex householdAddress = (AddressIndex)data.getHouseholdValue(householdIndex, HouseholdField.ADDRESS);
		if (householdAddress == null){
			String msg = "There is no address record for the selected household";
			notifyInsufficientAddress(msg);
			return;
		}
		displayMap(householdAddress, data);
	}

	@Override
	public void editMember() {
		throw new UnsupportedOperationException();
	}
}
