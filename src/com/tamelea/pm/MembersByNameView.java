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

import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.MemberNameFilter;
import com.tamelea.pm.data.MembersByNameTableModel;

@SuppressWarnings("serial")
public final class MembersByNameView extends View {
	private View							membersView;
	private JMenuBar						menuBar;
	
	public MembersByNameView(
			Data data, 
			PeriMeleonView membersView, 
			String sortFieldName, 
			MemberNameFilter filter) 
	{
		super("");
		this.data = data;
		this.membersView = membersView;
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
        com.tamelea.pm.PeriMeleonView.class.getResource("icon16.gif")));
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setTitle("Members whose name contains " + filter.getNameSearch());
		table = new PMTable();
		tableModel = new MembersByNameTableModel(table, data, sortFieldName, filter);
		table.setModel(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(200, 400));
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.getColumnModel().getColumn(1).setMinWidth(160);
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
	
	
	public void sizeAndPlace() {
		setVisible(true);
	    setSize(new Dimension(800, 500));
	    validate();
	    int x = membersView.getLocation().x + membersView.getWidth()/2 - getWidth()/2;
	    int y = membersView.getLocation().y + membersView.getHeight()/2 - getHeight()/2;
	    setLocation(x, y);
	    toFront();
	    requestFocus();
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
	
	public void editMember() {
		int tableRowIndex = table.getSelectedRow();
		if (tableRowIndex < 0) return;
		new EditMemberDialog(this, data, tableModel.getMemberIndex(tableRowIndex)).setVisible(true);
	}


	@Override
	public void editHousehold() {
		throw new UnsupportedOperationException();
	}
}
