package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;

import com.tamelea.pm.data.ActiveHouseholdFilter;
import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.AllMembersTableModel;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.HouseholdNameComparator;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.NamesHeaderTableModel;
import com.tamelea.pm.data.PMString;

@SuppressWarnings("serial")
public final class AllMembersView extends View {
	private AllMembersTableModel		tableModel;
	private PMTable						table;
	private NamesHeaderTableModel		headerModel;
	private PMTable						headerColumn;
	private Data						data;
	private PeriMeleonView				membersView;
	private JMenuBar					menuBar;
	private MembersGetMapAction 		membersGetMap;				
	
	AllMembersView(Data data, PeriMeleonView membersView) {
		super("All Members");
		this.data = data;
		this.membersView = membersView;
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
        com.tamelea.pm.PeriMeleonView.class.getResource("icon16.gif")));
		tableModel = new AllMembersTableModel(data);
		table = new PMTable(tableModel);
		headerModel = new NamesHeaderTableModel(data);
		headerColumn = new PMTable(headerModel);
		table.setSelectionModel(headerColumn.getSelectionModel());
		JViewport jv = new JViewport();
		jv.setView(headerColumn);
		headerColumn.getColumnModel().getColumn(0).setPreferredWidth(200);
		jv.setPreferredSize(headerColumn.getPreferredSize());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		//This obscure property prevents the columns from being resized
		//on a property change.
		table.setAutoCreateColumnsFromModel(false);

		JScrollPane jsp = new JScrollPane(table);
		jsp.setRowHeader(jv);
		jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, 
				headerColumn.getTableHeader());
		jsp.setPreferredSize(new Dimension(800, 300));
		this.getContentPane().add(jsp, BorderLayout.CENTER);
		addMenuBar();
		table.addMouseListener(new MembersMouseListener(this));
		headerColumn.addMouseListener(new MembersMouseListener(this));
	}
	
	private void addMenuBar() {
		JMenu membersMenu = new JMenu("Members");
		membersMenu.addMenuListener(new MembersMenuListener());
		membersMenu.add(new MembersNewAction());
		membersMenu.add(new MembersEditAction());
		membersMenu.add(new MembersRemoveAction());
		membersGetMap = new MembersGetMapAction();
		membersMenu.add(membersGetMap);
		menuBar = new JMenuBar();
		menuBar.add(membersMenu);
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
	
	public void editMember() {
		int tableRowIndex = headerColumn.getSelectedRow();
		if (tableRowIndex < 0) return;
		new EditMemberDialog(this, data, tableModel.getMemberIndex(tableRowIndex)).setVisible(true);
	}
	
	private final class MembersNewAction extends AbstractAction {

	    public MembersNewAction() {
			super("New member...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new AddMemberDialog(AllMembersView.this, data).setVisible(true);
		}
	}
	
	private final class MembersEditAction extends AbstractAction {

	    public MembersEditAction() {
			super("Edit member...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = table.getSelectedRow();
			if (tableIndex < 0) return;
			MemberIndex memberIndex = tableModel.getMemberIndex(tableIndex);
			new EditMemberDialog(AllMembersView.this, data, memberIndex).setVisible(true);
		}
	}
	
	private final class MembersRemoveAction extends AbstractAction {

	    public MembersRemoveAction() {
			super("Remove member...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = table.getSelectedRow();
			if (tableIndex < 0) return;
			MemberIndex memberIndex = tableModel.getMemberIndex(tableIndex);
			membersView.removeMember(AllMembersView.this, memberIndex);
		}
	}
	
	private final class MembersGetMapAction extends AbstractAction {

	    public MembersGetMapAction() {
			super("Show map to selected member's address");
		}
		
		public void actionPerformed(ActionEvent e) {
			showMap();
		}
	}
	
	public void showMap(){
		AddressIndex address;
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return;
		MemberIndex memberIndex = tableModel.getMemberIndex(tableIndex);
		AddressIndex tempAddress = 
			(AddressIndex)data.getMemberValue(memberIndex, MemberField.TEMP_ADDRESS);
		HouseholdIndex householdIndex = 
			(HouseholdIndex)data.getMemberValue(memberIndex, MemberField.HOUSEHOLD);
		AddressIndex householdAddress = (AddressIndex)data.getHouseholdValue(householdIndex, HouseholdField.ADDRESS);
		if (tempAddress == null){
			if (householdAddress == null){
				String msg = "There is no address record for the selected member";
				notifyInsufficientAddress(msg);
				return;
			}
			address = householdAddress;
		}else{
			address = tempAddress;
		}
		displayMap(address, data);
	}
	
	private final class MembersMenuListener implements MenuListener {

		public void menuCanceled(MenuEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void menuDeselected(MenuEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void menuSelected(MenuEvent e) {
			if (getSelectedMemberIndex() == null){
				membersGetMap.setEnabled(false);
			} else{
				membersGetMap.setEnabled(true);
			}
		}
		
		public MemberIndex getSelectedMemberIndex(){
			int tableIndex = table.getSelectedRow();
			if (tableIndex < 0) return null;
			return tableModel.getMemberIndex(tableIndex);
		}
	}

	@Override
	public void editHousehold() {
		throw new UnsupportedOperationException();
	}
}
