package com.tamelea.pm;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.tamelea.pm.data.Data;

public final class PeriMeleon {
	//Note use of Unicode "o with macron" for yet another touch of elitism!
	//test SVN commit e-mail notification: test 2
	public static final String applicationName = "PeriMele\u014dn";
	public static final String version = "version 1.4.1b (166)";
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat isoFormat =  new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat elegantFormat =  new SimpleDateFormat("MMMM d, yyyy");
	public static final String MEMBERS_VIEW_X = "MembersViewX";
	public static final String MEMBERS_VIEW_Y = "MembersViewY";
	public static final String MEMBERS_VIEW_WIDTH = "MembersViewWidth";
	public static final String MEMBERS_VIEW_HEIGHT = "MembersViewHeight";
	public static final String LAST_DIRECTORY = "LastDirectory";
	public static final String LAST_USED = "LastUsed";
	public static final String PASSWORD = "Password";
	private Data data;
	private PeriMeleonView view;
	private List<String> openFileList;
	private static Preferences preferences;
	private static String openDocName = null;
	static {
		preferences = Preferences.userNodeForPackage(PeriMeleon.class);
	}
	
	
	private PeriMeleon() {
		data = new Data();
		openFileList = new ArrayList<String>();
	}
	
	private void run() {
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", PeriMeleon.applicationName);
		new Splash();
		view = new PeriMeleonView(this, data);
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				setPlatformSpecificFeatures();	
				view.sizeAndPlace();
				view.setInitialData();
			}
		});
	}

	public static void main(String[] args) {
		 if (args.length > 0){
			 openDocName = args[0];
		 }
		 new PeriMeleon().run();
	}
	
	public static Preferences getPreferences() {
		return preferences;
	}
	
	public static OSName getOSName(){
		if (System.getProperty("os.name").startsWith("Windows")){
			return OSName.WINDOWS;
		} else if (System.getProperty("os.name").startsWith("Mac OS")){
			return OSName.MAC;
		} else{
			return OSName.OTHER;
		}
	}
	
	private void setPlatformSpecificFeatures(){
		if (PeriMeleon.getOSName() == OSName.MAC) {   
			new MacOSHandler(); 
	    } else if (PeriMeleon.getOSName() == OSName.WINDOWS){
	    	if (openDocName != null){
	    		openFileList.add(openDocName);
            	view.checkForOpenDocEventFile();
	    	}
	    }
	}
	
	private class MacOSHandler extends Application{
		
		@SuppressWarnings("deprecation")
		private MacOSHandler() {
			super();
			addApplicationListener(new AboutBoxHandler());
			addApplicationListener(new QuitBoxHandler());
			addApplicationListener(new OpenDocumentHandler());
	    }
		
		class AboutBoxHandler extends ApplicationAdapter {
	        public void handleAbout(ApplicationEvent event) {
	        	event.setHandled(true);
	            new Splash();
	        }
	    }
		class QuitBoxHandler extends ApplicationAdapter {
	        public void handleQuit(ApplicationEvent event) {
	            view.performExit();
	        }
	    }
		
		class OpenDocumentHandler extends ApplicationAdapter{
			public void handleOpenFile(ApplicationEvent event) {	
	            try {
	            	openFileList.add(new File(event.getFilename()).getCanonicalPath());
	            	view.checkForOpenDocEventFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					event.setHandled(true);
				}
	            
	        }
		}
	}
	
	public  List<String> getOpenFileList(){
		return openFileList;
	}
	
	public void clearOpenFileList(){
		openFileList.clear();
	}
}
