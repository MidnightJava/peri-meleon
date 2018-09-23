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

import com.tamelea.pm.data.BaptismsTableModel;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.PMDate;

@SuppressWarnings("serial")
public final class BaptismsView extends View {
	private BaptismsTableModel			tableModel;
	private PMTable						table;
	private PeriMeleonView				membersView;
	private JMenuBar					menuBar;
	
	public BaptismsView(Data data, PeriMeleonView membersView, PMDate earliest, PMDate latest) {
		super("Baptisms");
		this.membersView = membersView;
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
				com.tamelea.pm.PeriMeleonView.class.getResource("icon16.gif")));
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setTitle("Baptisms From " + earliest + " to " + latest + " Inclusive");
		tableModel = new BaptismsTableModel(data, earliest, latest);
		table = new PMTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(320);
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(450, 300));
		this.getContentPane().add(jsp, BorderLayout.CENTER);
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		labelPanel.add(new JLabel(
				"<html><b>NOTES:</b> You can copy the contents of this view to"
				+ " the system clipboard with Edit | Copy or with "
				+ (PeriMeleon.getOSName() == OSName.MAC ? "Command-C." : "Control-C.")
				+ "<br><i>This query returns only the baptisms for which a date is recorded.</i>"
				+ "</html>"), BorderLayout.CENTER);
		this.getContentPane().add(labelPanel, BorderLayout.NORTH);
		addMenuBar();
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
	
	public void sizeAndPlace() {
		setVisible(true);
		setSize(new Dimension(450, 300));
	    validate();
		java.awt.Point viewCenter = membersView.getCenter();
		java.awt.Dimension size = this.getSize();
		setLocation(Math.max(0, viewCenter.x - size.width / 2), 
				Math.max(0, viewCenter.y - size.height / 2));
	}
	
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			tableModel.removeListener();
		}
	}

	private void installActions(CopyAction copyAction) {
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( 
				KeyStroke.getKeyStroke(KeyEvent.VK_C,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), CopyAction.key);
		this.getRootPane().getActionMap().put(CopyAction.key,
				copyAction);
	}
	
	private final class CopyAction extends AbstractAction {
		public static final String key = "copyBaptisms";

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

	@Override
	public void editHousehold() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void editMember() {
		throw new UnsupportedOperationException();
	}
}
