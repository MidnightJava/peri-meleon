package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
//import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public final class Splash extends JWindow {
	private final Color background = new Color(225, 190, 105);
	
	public static Splash INSTANCE;

	  public Splash()
	  {
	    JLabel logo = new JLabel(new ImageIcon(this.getClass().getResource(
	        "splash.jpg")));
	    logo.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, background /*Color.black*/));
	    JLabel words = new JLabel();
	    words.setText("<html><font size=+1>Membership Database \"" + PeriMeleon.applicationName + "\" </font>" +
	                   "<br>" + PeriMeleon.version + "</html>");
	    words.setForeground(Color.black /*Color.lightGray*/);
	    JPanel stuffPanel = new JPanel();
	    stuffPanel.setBackground(background /*Color.black*/);
	    stuffPanel.setLayout(new BorderLayout());
	    stuffPanel.add(logo, BorderLayout.CENTER);
	    stuffPanel.add(words, BorderLayout.SOUTH);
	    JPanel contentPane = (JPanel)getContentPane();
	    //the colors for the bevels are derived from the background so don't set
	    //the background to black--or the borders become invisible!
	    contentPane.setBackground(background);
	    contentPane.setBorder(BorderFactory.createCompoundBorder(
	        BorderFactory.createCompoundBorder(
	          BorderFactory.createRaisedBevelBorder(),
	          BorderFactory.createLoweredBevelBorder()),
	        BorderFactory.createMatteBorder(5, 5, 5, 5, background /*Color.black*/)));
	    contentPane.setLayout(new BorderLayout(5, 5));
	    contentPane.add(stuffPanel, BorderLayout.CENTER);
	    pack();
	    Dimension screenSize =
	      Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension stuffSize = stuffPanel.getPreferredSize();
	    setLocation(screenSize.width/2 - (stuffSize.width/2),
	                screenSize.height/2 - (stuffSize.height/2));
	    addMouseListener(new MouseAdapter()
	    {
	      public void mouseReleased(MouseEvent e)
	      {
	        setVisible(false);
	        dispose();
	      }
	    });
	    setAlwaysOnTop(true);
	    setVisible(true);
	    INSTANCE = this;
	  }
	}
