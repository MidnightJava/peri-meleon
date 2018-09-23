package com.tamelea.pm;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.EnumMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.FieldEditor;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.Service;
import com.tamelea.pm.data.ServiceField;
import com.tamelea.pm.data.ServiceTableModel;
import com.tamelea.pm.data.Transaction;
import com.tamelea.pm.data.TransactionField;
import com.tamelea.pm.data.TransactionTableModel;

@SuppressWarnings("serial")
final class EditMemberDialog extends JDialog {
	private static final Dimension				tableSize = new Dimension(700, 50);
	private View								dialogParent;
	private Data								data;
	private JMenuBar							menuBar;
	private MemberIndex							member;
	private JPanel								insetPane;
	private EnumMap<MemberField, FieldEditor>	editors;
	private JButton								getMapButton;
	private JButton								closeButton;
	private JButton								cancelButton;
	private TransactionTableModel				transactionTableModel;
	private PMTable								transactionTable;
	private ServiceTableModel					serviceTableModel;
	private PMTable								serviceTable;
	private HouseholdIndex						previousHousehold;
	
	EditMemberDialog(View view, Data data, MemberIndex member) {
		super(view, "Edit Member", true);
		this.dialogParent = view;
		this.data = data;
		this.member = member;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		insetPane = new JPanel();
		insetPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		insetPane.setLayout(new SpringLayout());
		editors = new EnumMap<MemberField, FieldEditor>(MemberField.class);
		for (MemberField field : MemberField.values()) {
			if (field.editable) {
				insetPane.add(new JLabel(field.displayName));
				FieldEditor editor = null;
				Object storedValue = data.getMemberValue(member, field);
				if (field == MemberField.HOUSEHOLD) 
					previousHousehold = (HouseholdIndex)storedValue;
				try {
					Constructor<?> ctor = field.editorClass.getDeclaredConstructor(
							Window.class, Data.class, Object.class);
					editor = (FieldEditor) ctor.newInstance(this, data, storedValue);
				} catch (Exception e) {
					throw new IllegalStateException(
							"EditMemberDialog ctor: can't instantiate editor for "
									+ field, e);
				}
				editors.put(field, editor);
				insetPane.add(editor.getComponent());
			}			
		}
		SpringUtilities.makeCompactGrid(insetPane,
                editors.size(), 2,
                2, 2,
                2, 2);
		JPanel insetFlowPanel = new JPanel();
		insetFlowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		insetFlowPanel.add(insetPane);
		
		transactionTableModel = new TransactionTableModel(data, member);
		transactionTable = new PMTable(transactionTableModel);
		transactionTable.getTableHeader().setReorderingAllowed(false);
		transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		transactionTable.getSelectionModel().addListSelectionListener(new TransactionTableSelectionListener());
		JScrollPane transactionJsp = new JScrollPane(transactionTable);
		transactionJsp.setPreferredSize(tableSize);
		transactionJsp.setBorder(BorderFactory.createTitledBorder("Membership Transactions"));
		
		serviceTableModel = new ServiceTableModel(data, member);
		serviceTable = new PMTable(serviceTableModel);
		serviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serviceTable.getTableHeader().setReorderingAllowed(false);
//		serviceTable.getSelectionModel().addListSelectionListener(new ServiceTableSelectionListener());
		JScrollPane serviceJsp = new JScrollPane(serviceTable);
		serviceJsp.setPreferredSize(tableSize);
		serviceJsp.setBorder(BorderFactory.createTitledBorder("Service As Officer"));
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.add(transactionJsp);
		centerPanel.add(Box.createVerticalStrut(5));
		centerPanel.add(serviceJsp);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		getMapButton = new JButton("Get Map to Address");
		getMapButton.addActionListener(new GetMapButtonListener());
		southPanel.add(Box.createHorizontalStrut(157));
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

		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		westPanel.add(insetFlowPanel);
		westPanel.add(southPanel);
		westPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, 
				westPanel.getBackground()));
		this.add(westPanel, BorderLayout.WEST);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
		addMenuBar();
		
		String displayName = data.getMemberDisplayName(member);
		this.setTitle("Edit Member: " + displayName);
		
		pack();
		place();
	}
	
	private void addMenuBar() {
		menuBar = new JMenuBar();
		JMenu transactionMenu = new JMenu("Transactions");
		transactionMenu.add(new TransactionAddAction());
		transactionMenu.add(new TransactionEditAction());
		transactionMenu.add(new TransactionRemoveAction());
		menuBar.add(transactionMenu);
		JMenu serviceMenu = new JMenu("Officer Service");
		serviceMenu.add(new ServiceAddAction());
		serviceMenu.add(new ServiceEditAction());
		serviceMenu.add(new ServiceRemoveAction());
		menuBar.add(serviceMenu);
		this.setJMenuBar(menuBar);
	}
	
	private void place() {
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Point viewCenter = dialogParent.getCenter();
		java.awt.Dimension size = this.getSize();
		setLocation(Math.max(0, Math.min(screenSize.width - size.width, viewCenter.x - size.width / 2)), 
				Math.max(0, viewCenter.y - size.height / 2));
	}
	
	private boolean inputsAreValid() {
		for (MemberField field : editors.keySet()) {
			FieldEditor editor = editors.get(field);
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
				EnumMap<MemberField, Object> values = 
					new EnumMap<MemberField, Object>(MemberField.class);
				for (MemberField field : editors.keySet()) {
					FieldEditor editor = editors.get(field);
					Object datum = editor.getValue();
					values.put(field, datum);
					if (field == MemberField.HOUSEHOLD) {
						HouseholdIndex newHousehold = (HouseholdIndex)datum;
						if (!HouseholdIndex.equals(previousHousehold, newHousehold))
							warnOnHouseholdChange();
					}
//					System.out.println(field.toString() + ": " + datum);
				}
				data.setMemberValues(member, values);
				data.updateLastChange(member);
				transactionTableModel.removeListener();
				serviceTableModel.removeListener();
				setVisible(false);
			} 
		}
		
		private void warnOnHouseholdChange() {
			JOptionPane.showMessageDialog(
					EditMemberDialog.this, 
					"You have changed the assigned household."
					+ "\nBe sure to edit the affected households.", 
					"Warining: Household Changed", 
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancelButton.doClick();
		}
	}

	private final class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			transactionTableModel.removeListener();
			serviceTableModel.removeListener();
			setVisible(false);
		}
	}
	
	private final class GetMapButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dialogParent.showMap();
		}
	}
	
	private final class TransactionAddAction extends AbstractAction {

	    public TransactionAddAction() {
			super("Add transaction...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new AddTransactionDialog(dialogParent, data, member).setVisible(true);
		}
	}
	
	private final class TransactionEditAction extends AbstractAction {

	    public TransactionEditAction() {
			super("Edit transaction...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = transactionTable.getSelectedRow();
			if (tableIndex < 0) return;
			Transaction transaction = transactionTableModel.getTransaction(tableIndex);
			new EditTransactionDialog(dialogParent, data, member, transaction).setVisible(true);
		}
	}
	
	private final class TransactionRemoveAction extends AbstractAction {

	    public TransactionRemoveAction() {
			super("Remove transaction...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = transactionTable.getSelectedRow();
			if (tableIndex < 0) return;
			Transaction transaction = transactionTableModel.getTransaction(tableIndex);
			String message = "Remove transaction dated " + transaction.getValue(TransactionField.DATE) + " ?";
			int choice = JOptionPane.showConfirmDialog(
					EditMemberDialog.this, 
					message, 
					"Remove Transaction?", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.OK_OPTION) {
				data.removeTransaction(member, transaction);
			}
		}
	}
	
//	private final class TransactionTableSelectionListener implements ListSelectionListener {
//	    public void valueChanged(ListSelectionEvent e) {
//	    	ListSelectionModel lsm = (ListSelectionModel)e.getSource();
//	        int firstIndex = lsm.getMinSelectionIndex();
//	        if (e.getValueIsAdjusting()) return;
//	        if (firstIndex < 0) return;
//	        EditTransactionDialog dialog = new EditTransactionDialog(
//	        		view, data, member, transactionTableModel.getTransaction(firstIndex));
//	        dialog.setVisible(true);
//	    }
//	}
	
	private final class ServiceAddAction extends AbstractAction {

	    public ServiceAddAction() {
			super("Add officer service record...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new AddServiceDialog(dialogParent, data, member).setVisible(true);
		}
	}
	
	private final class ServiceEditAction extends AbstractAction {

	    public ServiceEditAction() {
			super("Edit officer service record...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = serviceTable.getSelectedRow();
			if (tableIndex < 0) return;
			Service service = serviceTableModel.getService(tableIndex);
			new EditServiceDialog(dialogParent, data, member, service).setVisible(true);
		}
	}
	
	private final class ServiceRemoveAction extends AbstractAction {

	    public ServiceRemoveAction() {
			super("Remove officer service record...");
		}
		
		public void actionPerformed(ActionEvent e) {
			int tableIndex = serviceTable.getSelectedRow();
			if (tableIndex < 0) return;
			Service service = serviceTableModel.getService(tableIndex);
			String message = "Remove officer service record dated " 
				+ service.getValue(ServiceField.DATE) + " ?";
			int choice = JOptionPane.showConfirmDialog(
					EditMemberDialog.this, 
					message, 
					"Remove Officer Service Record?", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.OK_OPTION) {
				data.removeService(member, service);
			}
		}
	}
	
//	private final class ServiceTableSelectionListener implements ListSelectionListener {
//	    public void valueChanged(ListSelectionEvent e) {
//	    	ListSelectionModel lsm = (ListSelectionModel)e.getSource();
//	        int firstIndex = lsm.getMinSelectionIndex();
//	        if (e.getValueIsAdjusting()) return;
//	        if (firstIndex < 0) return;
//	        EditServiceDialog dialog = new EditServiceDialog(
//	        		view, data, member, serviceTableModel.getService(firstIndex));
//	        dialog.setVisible(true);
//	    }
//	}
}
