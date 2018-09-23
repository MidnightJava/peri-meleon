package com.tamelea.pm;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.dom4j.DocumentException;

import com.apple.eio.FileManager;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.PhoneListMaker;
import com.tamelea.pm.nhpc.NHPCImportException;
import com.tamelea.pm.nhpc.NHPCImporter;
/**
 * The File menu.
 *
 */
@SuppressWarnings("serial")
final class FileEdit {
	private PeriMeleonView				view;
	private Data						data;
	private Encryption					encryption;
	private JMenu						fileMenu;
	private FileNewAction				fileNew;
	private FileOpenAction				fileOpen;
	private FileSaveAction				fileSave;
	private FileSaveAsAction			fileSaveAs;
	private FileSaveUnencryptedAction	fileSaveUnencrypted;
	@SuppressWarnings("unused")
	private FileExportDirectoryActionHTML	fileExportDirectoryHTML;
	private FileExportDirectoryActionPDF	fileExportDirectoryPDF;
	private FileExportPhoneListAction	fileExportPhoneList;
	private FileImportNHPCAction		fileImportNHPC;
	private FileExitAction				fileExit;
	
	FileEdit(PeriMeleonView view, Data data) {
		this.view = view;
		this.data = data;
		this.encryption = new Encryption();
	}
	
	public JMenu createMenu() {
		fileMenu = new JMenu("File");
		
		fileNew = new FileNewAction();
		fileMenu.add(fileNew);
		fileOpen = new FileOpenAction();
		fileMenu.add(fileOpen);
		fileSave = new FileSaveAction();
		fileMenu.add(fileSave);
		fileSaveAs = new FileSaveAsAction();
		fileMenu.add(fileSaveAs);
		fileSaveUnencrypted = new FileSaveUnencryptedAction();
		fileMenu.add(fileSaveUnencrypted);
		fileMenu.addSeparator();

		fileExportDirectoryPDF = new FileExportDirectoryActionPDF();
		fileMenu.add(fileExportDirectoryPDF);
//		fileExportDirectoryHTML = new FileExportDirectoryActionHTML();
//		fileMenu.add(fileExportDirectoryHTML);
		fileExportPhoneList = new FileExportPhoneListAction();
		fileMenu.add(fileExportPhoneList);
		fileMenu.addSeparator();

		fileImportNHPC = new FileImportNHPCAction();
		fileMenu.add(fileImportNHPC);
		fileMenu.addSeparator();
		
		if (!(PeriMeleon.getOSName() == OSName.MAC)){
			fileExit = new FileExitAction();
			fileMenu.add(fileExit);
		}
		return fileMenu;
	}
	
	public void setEnabled(boolean value) {
		fileOpen.setEnabled(value);
		fileSave.setEnabled(value);
		fileSaveAs.setEnabled(value);
	}
	
	private void openData() {
	    Object[] dialogLabels = { "Discard changes", "Cancel open" };
		if (data.isSaveNeeded()) {
			int choice = JOptionPane.showOptionDialog(view,
					"Data have been changed. "
					+ "You may:",
					"Data have been changed",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					dialogLabels,
					dialogLabels[1]);
			if (choice == JOptionPane.NO_OPTION) return;
		}
		view.getFileChooser().setFileFilter(new MembersPERFileFilter());
		view.getFileChooser().setCurrentDirectory(view.getMostRecentDirectory());
		view.getFileChooser().setSelectedFile(new File("members.per"));
		int returnVal = view.getFileChooser().showDialog(view, "Open Member Data");
		if (returnVal != JFileChooser.APPROVE_OPTION) return;
		File file = view.getFileChooser().getSelectedFile();
		data.removeAll();
		if (file.getName().contains(".xml")) openUnencryptedData(file);
		else openData(file, true);
	}
	
	public void openData(File file, boolean setCurrentDirectory){
		Object[] dialogLabels = { "Discard changes", "Cancel open" };
		if (data.isSaveNeeded()) {
			int choice = JOptionPane.showOptionDialog(view,
					"Data have been changed. "
					+ "You may:",
					"Data have been changed",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					dialogLabels,
					dialogLabels[1]);
			if (choice == JOptionPane.NO_OPTION) return;
		}
		boolean keepTrying = true;
		do {
			FileInputStream fis = null;
			try {
				Cipher cipher = encryption.makeDecryptionCipher(view.getPassword());
				if (cipher == null) return;
				fis = new FileInputStream(file);
				CipherInputStream inputStream = new CipherInputStream(fis, cipher);
				data.readAgain(inputStream);
				inputStream.close();
				data.setBoundFile(file);
				if (setCurrentDirectory){
					//This method was called via PeriMeleonView.checkForOPenDocEventFile(), which
					//is a non-EDT thread 
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
						}
					});
				}
				keepTrying = false;
			}
			catch (DocumentException ex) {
				//We assume here that, if the document fails at the outset, it's
				//a decrypt error. Other corruptions in the XML header could cause this.
				OpenPasswordDialog dialog = new OpenPasswordDialog(view, view.getPassword());
				dialog.setVisible(true);
				
				if (dialog.getResult() != JOptionPane.YES_OPTION) keepTrying = false; //canceled
				else {
					char[] newPassword = dialog.getValue();
					view.setPassword(newPassword);
					keepTrying = true;
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(view,
						"Open of member data encountered problem:\n" 
						+ ex.getMessage(),
						"Open Data Problem",
						JOptionPane.WARNING_MESSAGE);
				keepTrying = false;
			}
			finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException ignored) { }
			}
		} while (keepTrying);
	}
	
	public void openUnencryptedData(File file) {
		Object[] dialogLabels = { "Discard changes", "Cancel open" };
		if (data.isSaveNeeded()) {
			int choice = JOptionPane.showOptionDialog(view,
					"Data have been changed. "
					+ "You may:",
					"Data have been changed",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					dialogLabels,
					dialogLabels[1]);
			if (choice == JOptionPane.NO_OPTION) return;
		}
		boolean keepTrying = true;
		do {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				data.readAgain(fis);
				fis.close();
				data.setBoundFile(file);
				keepTrying = false;
			}
			catch(Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(view,
						"Open of member data encountered problem:\n" 
						+ ex.getMessage(),
						"Open Data Problem",
						JOptionPane.WARNING_MESSAGE);
				keepTrying = false;
			}
			finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException ignored) { }
			}
		} while (keepTrying);
	}
	
	/**
	 * Used only when app is first opened
	 * @param fileString
	 */
	public void openInitialData(File file){
		try {
			Cipher cipher = encryption.makeDecryptionCipher(view.getPassword());
			if (cipher == null) return;
			FileInputStream fis = new FileInputStream(file);
			CipherInputStream inputStream = new CipherInputStream(fis, cipher);
			data.readAgain(inputStream);
			inputStream.close();
			data.setBoundFile(file);
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(view,
					"Open of " + file + " encountered problem:\n" 
					+ ex.getMessage(),
					"Open Data Problem",
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void exportDirectoryHTML() {
		String suggestedName = PeriMeleon.isoFormat.format(new Date()) + "-directory.html";
		File file = chooseOutputFile(new MembersHTMLFileFilter(), suggestedName, "Export Directory");
		if (file == null) return;
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			new DirectoryExporterHTML(data, new PrintStream(outputStream)).export();
			outputStream.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view,
					"Export failed: " + ex.getMessage()
					+ " (" + ex.getClass().getSimpleName() + ")",
					"Export Failed",
					JOptionPane.ERROR_MESSAGE);
		}
		view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
	}
	
	private void exportDirectoryPDF() {
		String suggestedName = PeriMeleon.isoFormat.format(new Date()) + "-directory.pdf";
		File file = chooseOutputFile(new MembersPDFFileFilter(), suggestedName, "Export Directory");
		if (file == null) return;
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			new DirectoryExporterPDF(data, outputStream).export();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view,
					"Export failed: " + ex.getMessage()
					+ " (" + ex.getClass().getSimpleName() + ")",
					"Export Failed",
					JOptionPane.ERROR_MESSAGE);
		}
		finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException ignored) { }
			}
		}
		view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
	}
	
	private void exportPhoneList() {
		File file = chooseOutputFile(new MembersCSVFileFilter(), "phone-list.csv", "Export Phone List");
		if (file == null) return;
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			new PhoneListMaker(data, new PrintStream(outputStream), 14).make();
			outputStream.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view,
					"Export failed: " + ex.getMessage()
					+ " (" + ex.getClass().getSimpleName() + ")",
					"Export Failed",
					JOptionPane.ERROR_MESSAGE);
		}
		view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
	}
	
	private void importNHPCData() {
	    Object[] dialogLabels = { "Discard changes", "Cancel import" };
		if (data.isSaveNeeded()) {
			int choice = JOptionPane.showOptionDialog(view,
					"Data have been changed. "
					+ "You may:",
					"Data have been changed",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					dialogLabels,
					dialogLabels[1]);
			if (choice == JOptionPane.NO_OPTION) return;
		}
		view.getFileChooser().setFileFilter(new MembersCSVFileFilter());
		view.getFileChooser().setCurrentDirectory(view.getMostRecentDirectory());
		view.getFileChooser().setSelectedFile(new File("roll.csv"));
		int returnVal = view.getFileChooser().showDialog(view, "Import NHPC Data");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			data.removeAll();
			try {
				File file = view.getFileChooser().getSelectedFile();
				FileReader fileIn = new FileReader(file);
				LineNumberReader reader = new LineNumberReader(fileIn);
				NHPCImporter.read(data, reader);
				reader.close();
			}
			catch(NHPCImportException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(view,
						"Import of NHPC data encountered problem at line " 
						+ ex.getLineNumber() + ":\n" 
						+ ex.getMessage(),
						"Import Data Problem",
						JOptionPane.WARNING_MESSAGE);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(view,
						"Import of NHPC data encountered problem:\n" 
						+ ex.getMessage(),
						"Import Data Problem",
						JOptionPane.WARNING_MESSAGE);
			}
			view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
		}
	}

	private void saveData() {
		File file = data.getBoundFile();
		if (file == null) {
			saveDataAs();
			return;
		}
		try {
			Cipher cipher = encryption.makeEncryptionCipher(view.getPassword());
			if (cipher == null) return;
			FileOutputStream fos = new FileOutputStream(file);
			CipherOutputStream outputStream = new CipherOutputStream(fos, cipher);
			data.save(outputStream);
			outputStream.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view,
					"Save failed: " + ex.getMessage()
					+ " (" + ex.getClass().getSimpleName() + ")",
					"Save Failed",
					JOptionPane.ERROR_MESSAGE);
		}
		view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
	}

	private void saveDataAs() {
		File file = chooseOutputFile(new MembersPERFileFilter(), "members.per", "Save Members");
		if (file == null) return;
		try {
			SavePasswordDialog dialog = new SavePasswordDialog(view, view.getPassword());
			dialog.setVisible(true);
			if (dialog.getResult() != JOptionPane.YES_OPTION) return;
			char[] newPassword = dialog.getValue();
			view.setPassword(newPassword);
			Cipher cipher = encryption.makeEncryptionCipher(newPassword);
			if (cipher == null) return;
			FileOutputStream fos = new FileOutputStream(file);
			CipherOutputStream outputStream = new CipherOutputStream(fos, cipher);
			data.save(outputStream);
			outputStream.close();
			data.setBoundFile(file);
			if (PeriMeleon.getOSName() == OSName.MAC){
				final String CREATOR_CODE = "504D4C4E";//ASCII PMLN
				final String FILE_TYPE_CODE = "504D4C44";//ASCII PMLD
				FileManager.setFileCreator(file.getCanonicalPath(), Integer.parseInt(CREATOR_CODE, 16));
				FileManager.setFileType(file.getCanonicalPath(), Integer.parseInt(FILE_TYPE_CODE, 16));
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view,
					"Save failed: " + ex.getMessage()
					+ " (" + ex.getClass().getSimpleName() + ")",
					"Save Failed",
					JOptionPane.ERROR_MESSAGE);
		}
		view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
	}
	
	private File chooseOutputFile(
			FileFilter filter,
			String defaultName,
			String chooserTitle)
	{
		Object[] overwriteOptions = { "Overwrite file", "Choose another" };
		boolean fileChosen = false;
		boolean operationCancelled = false;
		File file = null;
		view.getFileChooser().setCurrentDirectory(view.getMostRecentDirectory());
		do {
			view.getFileChooser().setFileFilter(filter);
			view.getFileChooser().setSelectedFile(new File(defaultName));
			int returnVal = view.getFileChooser().showDialog(
					view, chooserTitle);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				operationCancelled = false;
				file = view.getFileChooser().getSelectedFile();
				if (file.exists()) {
					int choice = JOptionPane.showOptionDialog(view,
							"File " + file.getName() + " exists. Overwrite it?",
							"Overwite file?",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							overwriteOptions,
							overwriteOptions[0]);
					if (choice == JOptionPane.YES_OPTION) {
						fileChosen = true;
					}
					else {
						fileChosen = false;
					}
				}
				else {
					fileChosen = true;
					operationCancelled = false;
				}
			}
			else {
				fileChosen = true;
				operationCancelled = true;
			}
		} while(!fileChosen);
		if (operationCancelled) return null;
		return file;
	}

	private void saveDataUnencrypted() {
		File file = chooseOutputFile(
				new MembersXMLFileFilter(), 
				"members.xml", 
				"Save Members Unencrypted");
		if (file == null) return;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			data.save(fos);
			fos.close();
			data.setBoundFile(file);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(view,
					"Save failed: " + ex.getMessage()
					+ " (" + ex.getClass().getSimpleName() + ")",
					"Save Failed",
					JOptionPane.ERROR_MESSAGE);
		}
		view.setMostRecentDirectory(view.getFileChooser().getCurrentDirectory());
	}
	
	private final class FileNewAction extends AbstractAction {
	    Object[] dialogLabels = { "Discard changes", "Cancel open" };

	    public FileNewAction() {
			super("New database...");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		public void actionPerformed(ActionEvent e) {
			if (data.isSaveNeeded()) {
				int choice = JOptionPane.showOptionDialog(
						view,
						"Data have been changed. "
						+ "You may:",
						"Data have been changed",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						dialogLabels,
						dialogLabels[1]);
				if (choice == JOptionPane.NO_OPTION) return;
			}
			data.removeAll();
		}
	}
	
	private final class FileOpenAction extends AbstractAction {

	    public FileOpenAction() {
			super("Open data...");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		public void actionPerformed(ActionEvent e) {
//			if (data.getSaveNeeded()) {
//				int choice = JOptionPane.showOptionDialog(
//						view,
//						"Data have been changed. "
//						+ "You may:",
//						"Data have been changed",
//						JOptionPane.YES_NO_OPTION,
//						JOptionPane.WARNING_MESSAGE,
//						null,
//						dialogLabels,
//						dialogLabels[1]);
//				if (choice == JOptionPane.NO_OPTION) return;
//			}
			openData();
		}
	}
	
	private final class FileSaveAction extends AbstractAction {
	    public FileSaveAction() {
			super("Save");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		public void actionPerformed(ActionEvent e) {
			saveData();
		}
	}
	
	private final class FileSaveAsAction extends AbstractAction {
	    public FileSaveAsAction() {
			super("Save as...");
		}
		
		public void actionPerformed(ActionEvent e) {
			saveDataAs();
		}
	}
	
	private final class FileSaveUnencryptedAction extends AbstractAction {
	    public FileSaveUnencryptedAction() {
			super("Save unencrypted...");
		}
		
		public void actionPerformed(ActionEvent e) {
			saveDataUnencrypted();
		}
	}
	
	private final class FileExportDirectoryActionHTML extends AbstractAction {
	    @SuppressWarnings("unused")
		public FileExportDirectoryActionHTML() {
			super("Export directory as HTML...");
			//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
		}
		
		public void actionPerformed(ActionEvent e) {
			exportDirectoryHTML();
		}
	}
	
	private final class FileExportDirectoryActionPDF extends AbstractAction {
	    public FileExportDirectoryActionPDF() {
			super("Export directory as PDF...");
			//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
		}
		
		public void actionPerformed(ActionEvent e) {
			exportDirectoryPDF();
		}
	}
	
	private final class FileExportPhoneListAction extends AbstractAction {
	    public FileExportPhoneListAction() {
			super("Export phone list...");
			//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
		}
		
		public void actionPerformed(ActionEvent e) {
			exportPhoneList();
		}
	}
	
	private final class FileImportNHPCAction extends AbstractAction {
	    public FileImportNHPCAction() {
			super("Import NHPC data...");
			//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
		}
		
		public void actionPerformed(ActionEvent e) {
			importNHPCData();
		}
	}

	private final class FileExitAction extends AbstractAction {
		public FileExitAction() {
			super("Exit");
			//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
		}
		
		public void actionPerformed(ActionEvent e) {
			view.performExit();
		}
	}
}
