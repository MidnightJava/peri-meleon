package com.tamelea.pm;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.PeriMeleonViewTableModel;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.PMString;

@SuppressWarnings("serial")
final class PeriMeleonView extends View implements PropertyChangeListener {
	private PeriMeleon					application;
	private char[]						password;
	private JFileChooser				fileChooser;
	private File						mostRecentDirectory;
	private JMenuBar					menuBar;
	private FileEdit					fileEdit;
	private MembersEdit					membersEdit;
	private HouseholdsEdit				householdsEdit;
	private QueriesEdit					queriesEdit;
	private JMenu						helpMenu;
	private JMenuItem					helpHelp;
	private JMenuItem					helpAbout;
	private HelpSet						helpSet;
	private HelpBroker					helpBroker;
	private static final String titlePrefix = PeriMeleon.applicationName + " ";

	PeriMeleonView(PeriMeleon application, Data data) {
		super("PeriMeleon");
		this.application = application;
		this.data = data;
		setLookAndFeel();
		setUpFileChooser();
		setUpHelp();
		constructDisplay();
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setTitle(refreshTitle());
		password = PeriMeleon.getPreferences().get(PeriMeleon.PASSWORD, "").toCharArray();
		data.addPropertyChangeListener(this);
		table.addMouseListener(new MembersMouseListener(this));
	}
	
	private void constructDisplay() {
		this.setIconImage(Toolkit.getDefaultToolkit().createImage(
		        com.tamelea.pm.PeriMeleonView.class.getResource("icon16.gif")));
		this.getContentPane().setLayout(new BorderLayout());
		tableModel = new PeriMeleonViewTableModel(data);
		table = new PMTable();
		table.setModel((PeriMeleonViewTableModel)tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		//This obscure property prevents the columns from being resized
		//on a property change.
		table.setAutoCreateColumnsFromModel(false);
		JScrollPane jsp = new JScrollPane(table);
		this.getContentPane().add(jsp, BorderLayout.CENTER);
		addMenuBar();
	}
	
	private void addMenuBar() {
		fileEdit = new FileEdit(this, data);
		membersEdit = new MembersEdit(this, data);
		householdsEdit = new HouseholdsEdit(this, data);
		queriesEdit = new QueriesEdit(this, data);
		
		helpMenu = new JMenu("Help");
		helpHelp = new JMenuItem("Help...");
		helpHelp.addActionListener(new CSH.DisplayHelpFromSource(helpBroker));
		helpMenu.add(helpHelp);
		helpAbout = new JMenuItem("About");
		helpAbout.addActionListener(new HelpAboutListener());
		helpMenu.add(helpAbout);
		
		menuBar = new JMenuBar();
		menuBar.add(fileEdit.createMenu());
		menuBar.add(membersEdit.createMenu());
		menuBar.add(householdsEdit.createMenu());
		menuBar.add(queriesEdit.createMenu());
		menuBar.add(helpMenu);
		this.setJMenuBar(menuBar);
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
	    int x = PeriMeleon.getPreferences().getInt(
	    		PeriMeleon.MEMBERS_VIEW_X, screenSize.width - getSize().width);
	    x = Math.max(0, x);
	    int y = PeriMeleon.getPreferences().getInt(PeriMeleon.MEMBERS_VIEW_Y, 0);
	    y = Math.max(0, y);
	    setLocation(x, y);
	    toFront();
	}
	
	void setInitialData() {
		String lastUsed = PeriMeleon.getPreferences().get(PeriMeleon.LAST_USED, null);
		if (lastUsed == null || lastUsed.equals("null")) return;
		fileEdit.openInitialData(new File(lastUsed));
	}
	
	private void setLookAndFeel(){
		String lafClassName;
		if (PeriMeleon.getOSName() == OSName.WINDOWS){
			lafClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
			MetalLookAndFeel.setCurrentTheme(new OceanTheme());
		} else if (PeriMeleon.getOSName() == OSName.MAC){
			lafClassName = "apple.laf.AquaLookAndFeel";
		} else {
			lafClassName = UIManager.getSystemLookAndFeelClassName();
		}
		try {
			UIManager.setLookAndFeel(lafClassName);
		} catch (Exception e1) {}
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	private String refreshTitle() {
		StringBuffer title = new StringBuffer(titlePrefix);
		title.append("[");
		File dataFile = data.getBoundFile();
		title.append((dataFile == null) ? "new data" : dataFile.getName());
		if (data.isSaveNeeded()) title.append("*");
		title.append("]");
		return title.toString();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Data.BOUND_FILE)
				|| e.getPropertyName().equals(Data.SAVE_NEEDED)) {
			setTitle(refreshTitle());
		}
	}
	
	protected void processWindowEvent(WindowEvent e) {
		//super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			performExit();
		}
	}
	
	public void editMember() {
		int tableRowIndex = table.getSelectedRow();
		if (tableRowIndex < 0) return;
		new EditMemberDialog(this, data, tableModel.getMemberIndex(tableRowIndex)).setVisible(true);
	}
	
	void copyMember() {
		int tableRowIndex = table.getSelectedRow();
		if (tableRowIndex < 0) return;
		String contactInfo = data.getContactInfo(tableModel.getMemberIndex(tableRowIndex));
		StringSelection ss = new StringSelection(contactInfo);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	}
	
	void removeMember(View dialogParent) {
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return;
		MemberIndex index = tableModel.getMemberIndex(tableIndex);
		removeMember(dialogParent, index);
	}
	
	void removeMember(View dialogParent, MemberIndex index) {
		String name = data.getMemberDisplayName(index);
		int selection = JOptionPane.showConfirmDialog(
				dialogParent, 
				"This will permanently remove " + name + " from the database.", 
				"Remove Member?", 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		if (selection == JOptionPane.OK_OPTION) data.removeMember(index);
	}
	
	public MemberIndex getSelectedMemberIndex(){
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return null;
		return tableModel.getMemberIndex(tableIndex);
	}
	
	void editHousehold(View dialogParent) {
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return;
		MemberIndex memberIndex = tableModel.getMemberIndex(tableIndex);
		HouseholdIndex householdIndex = (
				HouseholdIndex)data.getMemberValue(memberIndex, MemberField.HOUSEHOLD);
		if (householdIndex == null) return; //no household assigned
		new EditHouseholdDialog(dialogParent, data, householdIndex).setVisible(true);
	}
	
	void removeHousehold(View dialogParent) {
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return;
		MemberIndex memberIndex = tableModel.getMemberIndex(tableIndex);
		HouseholdIndex householdIndex = (
				HouseholdIndex)data.getMemberValue(memberIndex, MemberField.HOUSEHOLD);
		if (householdIndex == null) return; //no household assigned
		removeHousehold(dialogParent, householdIndex);
	}
	
	void removeHousehold(View dialogParent, HouseholdIndex householdIndex) {
		List<String> refugees = data.getNamesInHousehold(householdIndex);
		String householdName = ((PMString)
				data.getHouseholdValue(householdIndex, HouseholdField.NAME)).toString();
		if (!refugees.isEmpty()) {
			String illTidings = "Removing household \"" + householdName 
				+ "\" will leave the following homeless:";
			for (String name : refugees) illTidings += "\n" + name;
			int choice = JOptionPane.showConfirmDialog(
					dialogParent, 
					illTidings, 
					"Remove household?", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.OK_OPTION) {
				data.removeHousehold(householdIndex);
			}
		} else {
			int choice = JOptionPane.showConfirmDialog(
					dialogParent,
					"Remove household \"" + householdName + "\"?",
					"Remove household?", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.OK_OPTION) {
				data.removeHousehold(householdIndex);
			}

		}
	}
	
	void performExit() {
	    Object[] dialogLabels = { "Discard changes and exit", "Cancel exit" };
	    if (data.isSaveNeeded()) {
			int choice = JOptionPane.showOptionDialog(this,
					"The data have unsaved changes: "
					+ "\nYou may:",
					"Data have been changed",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					dialogLabels,
					dialogLabels[1]);
			if (choice == JOptionPane.NO_OPTION) return;
		}
	    PeriMeleon.getPreferences().put(PeriMeleon.MEMBERS_VIEW_X, Integer.toString(getX()));
	    PeriMeleon.getPreferences().put(PeriMeleon.MEMBERS_VIEW_Y, Integer.toString(getY()));
	    PeriMeleon.getPreferences().put(PeriMeleon.MEMBERS_VIEW_WIDTH, Integer.toString(getWidth()));
	    PeriMeleon.getPreferences().put(PeriMeleon.MEMBERS_VIEW_HEIGHT, Integer.toString(getHeight()));
	    PeriMeleon.getPreferences().put(PeriMeleon.PASSWORD, new String(password));
	    File boundFile = data.getBoundFile();
	    PeriMeleon.getPreferences().put(PeriMeleon.LAST_USED, (boundFile == null) ? "null" : boundFile.toString());
	    System.exit(0);
	}

	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	public void setMostRecentDirectory(File mostRecentDirectory) {
		this.mostRecentDirectory = mostRecentDirectory;
		PeriMeleon.getPreferences().put(PeriMeleon.LAST_DIRECTORY, mostRecentDirectory.toString());
	}

	public File getMostRecentDirectory() {
		return mostRecentDirectory;
	}
	
	private void setUpFileChooser() {
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new MembersXMLFileFilter());
		String lastDirectoryDefault;
		if (PeriMeleon.getOSName() == OSName.WINDOWS)
			lastDirectoryDefault = System.getProperty("user.dir");
		else lastDirectoryDefault = System.getProperty("user.home");
		String lastDirectoryString = PeriMeleon.getPreferences().get(
				PeriMeleon.LAST_DIRECTORY, lastDirectoryDefault);
		setMostRecentDirectory(new File(lastDirectoryString));
//		if (System.getProperty("os.name").contains("Windows"))
//			setMostRecentDirectory(new File(System.getProperty("user.dir")));
//		else setMostRecentDirectory(new File(System.getProperty("user.home")));
	}
	
	private void setUpHelp() {
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			URL helpSetURL = HelpSet.findHelpSet(loader, "help/MembersHelp.hs");
			helpSet = new HelpSet(null, helpSetURL);
			helpBroker = helpSet.createHelpBroker();
		}
		catch (Exception e) {
			System.out.println("help set not found: " + e.getMessage());
		}
	}

	private final class HelpAboutListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new Splash();
		}
	}
	
	char[] getPassword() {
		return password;
	}

	void setPassword(char[] password) {
		this.password = password;
	    PeriMeleon.getPreferences().put(PeriMeleon.PASSWORD, new String(password));
	}
	
	public void launchView(Class<?> viewClass, Object... args){
		Constructor<?> cons;
		Class<?>[] argClasses = getClasses(args);
		try {
			cons = viewClass.getConstructor(argClasses);
			Object instance = cons.newInstance(args);
			Method sizeAndPlace = instance.getClass().getMethod("sizeAndPlace");
			sizeAndPlace.invoke(instance, (Object[]) null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.getTargetException().printStackTrace();
		}
		
	}
	
	//This SuppressWarnings isn't optional: there's no way around it.
	//See The Java Prog Lang, 4th ed., p. 745.
	@SuppressWarnings("unchecked")
	private Class[] getClasses(Object[] objects){
		Class[] classes = new Class[objects.length];
		for(int i = 0; i < objects.length; i++){
			classes[i] = (Class)(objects[i].getClass());
		}
		return classes;
	}
	
	/* must be invoked after addMenuBar(), or fileList will be null */
	public void checkForOpenDocEventFile(){
		List<String> fileList = application.getOpenFileList();
		if (fileList.size() == 0) return;
		final String fileName = fileList.get(0);
		application.clearOpenFileList();
		//Since this method may launch a JDialog, and it can be called from the Event Dispatch Thread
		//(via PeriMeleon.OPenDocHandler.hanleOPenFile()), we spawn a new thread, to avoid blocking on the EDT
		new Runnable(){
			public void run() {
				fileEdit.openData(new File(fileName), false);
			}
		}.run();
	}

	@Override
	public void editHousehold() {
		throw new UnsupportedOperationException();
	}
	
//	private final class MainTableSelectionListener implements ListSelectionListener {
//	    public void valueChanged(ListSelectionEvent e) {
//	    	ListSelectionModel lsm = (ListSelectionModel)e.getSource();
//	        int firstIndex = lsm.getMinSelectionIndex();
//	        if (e.getValueIsAdjusting()) return;
//	        if (firstIndex < 0) return;
//	        EditMemberDialog dialog = new EditMemberDialog(
//	        		PeriMeleonView.this, data, tableModel.getMemberIndex(firstIndex));
//	        dialog.setVisible(true);
//	    }
//	}
}
