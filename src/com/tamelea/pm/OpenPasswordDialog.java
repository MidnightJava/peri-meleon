package com.tamelea.pm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

@SuppressWarnings("serial")
final class OpenPasswordDialog extends JDialog {
	private PeriMeleonView view;
	private JPasswordField password1;
	private JButton closeButton;
	private JButton cancelButton;
	private int result;
	private char[] value;
	
	OpenPasswordDialog(PeriMeleonView view, char[] initialPassword) {
		super(view, "Enter Password", true);
		this.view = view;

		password1 = new JPasswordField(new String(initialPassword), 10);
		password1.setCaretPosition(0);
		password1.moveCaretPosition(initialPassword.length);
		JPanel topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(new JLabel("Type password:"));
		topPanel.add(Box.createVerticalStrut(5));
		topPanel.add(password1);
		JPanel topFlowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		topFlowPanel.add(topPanel);
		
		closeButton = new JButton("Submit");
		closeButton.addActionListener(new CloseButtonListener());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonListener());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(closeButton);
		buttons.add(cancelButton);
		
		setLayout(new BorderLayout());
		this.add(topFlowPanel, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		pack();
		place();
	}
	
	private void place() {
		java.awt.Point viewCenter = view.getCenter();
		java.awt.Dimension size = this.getSize();
		setLocation(Math.max(0, viewCenter.x - size.width / 2), 
				Math.max(0, viewCenter.y - size.height / 2));
	}
	
	int getResult() {
		return result;
	}
	
	char[] getValue() {
		return value;
	}

	private final class CloseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			value = password1.getPassword();
			result = JOptionPane.YES_OPTION;
			OpenPasswordDialog.this.dispose();
		}
		
	}

	private final class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			result = JOptionPane.CANCEL_OPTION;
			OpenPasswordDialog.this.dispose();
		}
		
	}
}
