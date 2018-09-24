package com.tamelea.pm;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.MemberStatus;
import com.tamelea.pm.data.MembersByStatusTableModel;
import com.tamelea.pm.data.ResidenceSelector;

@SuppressWarnings("serial")
public final class MembersByStatusView extends View {
	private MembersByStatusTableModel	tableModel;
	private Data						data;
	private PeriMeleonView				membersView;
	private JMenuBar					menuBar;
	
	MembersByStatusView(Data data, PeriMeleonView membersView, MemberStatus status, ResidenceSelector rs, String nameSearch) {
		super(status.toString());
		this.data = data;
		this.membersView = membersView;
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
        com.tamelea.pm.PeriMeleonView.class.getResource("icon16.gif")));
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setTitle(status.toString() + " Members " + 
				(rs == ResidenceSelector.BOTH ?"Including " : "Who Are  ") + rs.toString());
		tableModel = new MembersByStatusTableModel(data, status, rs, nameSearch);
		table = new PMTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(200, 400));
		this.getContentPane().add(jsp, BorderLayout.CENTER);
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		labelPanel.add(new JLabel(
				"<html><b>NOTE:</b> You can copy the contents of this view to"
				+ " the system clipboard with Edit | Copy or with "
				+ (PeriMeleon.getOSName() == OSName.MAC ? "Command-C." : "Control-C.")
				+ " You may then paste the clipboard contents directly into Excel, or"
				+ " paste the contents into a file, which will be a tab-delimited representation"
				+ " of the information from the table."
				+ "</html>"), BorderLayout.CENTER);
		this.getContentPane().add(labelPanel, BorderLayout.NORTH);
		addMenuBar();
		table.addMouseListener(new MembersMouseListener(this));
	}
	
	
	void sizeAndPlace() {
		setVisible(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int screenWidth = (screenSize.width > 1024) ? (int)(screenSize.width * 0.9)
	    		: screenSize.width;
	    int width = PeriMeleon.getPreferences().getInt(
	    		PeriMeleon.MEMBERS_VIEW_WIDTH, screenWidth);
	    int height = PeriMeleon.getPreferences().getInt(
	    		PeriMeleon.MEMBERS_VIEW_HEIGHT, screenSize.height / 2);
	    setSize(width, height);
	    validate();
	    int x = membersView.getLocation().x + 30;
	    int y = membersView.getLocation().y + 55;
	    setLocation(x, y);
	    toFront();
	}
	
	public void editMember() {
		int tableRowIndex = table.getSelectedRow();
		if (tableRowIndex < 0) return;
		new EditMemberDialog(this, data, tableModel.getMemberIndex(tableRowIndex)).setVisible(true);
	}
	
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			tableModel.removeListener();
		}
	}
	
	private void addMenuBar() {
		JMenu editMenu = new JMenu("Edit");
		CopyAction copyAction = new CopyAction();
		editMenu.add(copyAction);
		installActions(copyAction);
		menuBar = new JMenuBar();
		menuBar.add(editMenu);
		this.setJMenuBar(menuBar);
	}
	
	private void installActions(CopyAction copyAction) {
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( 
				KeyStroke.getKeyStroke(KeyEvent.VK_C,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), CopyAction.key);
		this.getRootPane().getActionMap().put(CopyAction.key,
				copyAction);
	}
	
	private final class CopyAction extends AbstractAction {
		public static final String key = "copyBirthdays";

	    public CopyAction() {
			super("Copy");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		public void actionPerformed(ActionEvent e) {
			String toBeCopied = tableModel.getTSVText();
			if (toBeCopied.length() < 1) return;
			StringSelection ss = new StringSelection(toBeCopied);
	        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
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


	@Override
	public void editHousehold() {
		throw new UnsupportedOperationException();
	}
}

