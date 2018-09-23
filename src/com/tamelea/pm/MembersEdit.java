package com.tamelea.pm;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.tamelea.pm.data.Data;
/**
 * The Members menu.
 *
 */
@SuppressWarnings("serial")
final class MembersEdit {
	private PeriMeleonView			view;
	private Data				data;
	private JMenu				membersMenu;
	private MembersViewAction	membersView;
	private MembersNewAction	membersNew;
	private AddMemberDialog		addMemberDialog;
	private MembersEditAction	membersEdit;
	private MembersRemoveAction	membersRemove;
	private MembersCopyAction   membersCopy;
	private MembersGetMapAction membersGetMap;
	private AllMembersView		allMembersView;
	
	MembersEdit(PeriMeleonView view, Data data) {
		this.view = view;
		this.data = data;
		this.allMembersView = null;
		this.addMemberDialog = null;
	}
	
	public JMenu createMenu() {
		membersMenu = new JMenu("Members");
		membersView = new MembersViewAction();
		membersMenu.add(membersView);
		membersNew = new MembersNewAction();
		membersMenu.add(membersNew);
		membersEdit = new MembersEditAction();
		membersMenu.add(membersEdit);
		membersRemove = new MembersRemoveAction();
		membersMenu.add(membersRemove);
		membersCopy = new MembersCopyAction();
		membersMenu.add(membersCopy);
		membersGetMap = new MembersGetMapAction();
		membersMenu.add(membersGetMap);
		membersMenu.addMenuListener(new MembersMenuListener());
		return membersMenu;
	}
	
	private final class MembersViewAction extends AbstractAction {

	    public MembersViewAction() {
			super("View all members...");
		}
		
	    /**
	     * Remembering the view here means that wherever the user moved it to before,
	     * it will appear there when made visible again.
	     * It also avoids the problem of the table models in the view needing to
	     * remove themselves as property listeners when the view is disposed of.
	     */
		public void actionPerformed(ActionEvent e) {
			if (allMembersView == null) {
				allMembersView = new AllMembersView(data, view);;
				allMembersView.sizeAndPlace();
			}
			allMembersView.setVisible(true);
		}
	}
	
	private final class MembersNewAction extends AbstractAction {

	    public MembersNewAction() {
			super("New member...");
		}
		
		public void actionPerformed(ActionEvent e) {
			if (addMemberDialog == null || !addMemberDialog.isVisible()) {
				addMemberDialog = new AddMemberDialog(view, data);
			}
			addMemberDialog.setVisible(true);
		}
	}
	
	private final class MembersEditAction extends AbstractAction {

	    public MembersEditAction() {
			super("Edit member...");
		}
		
		public void actionPerformed(ActionEvent e) {
			view.editMember();
		}
	}
	
	private final class MembersCopyAction extends AbstractAction {

	    public MembersCopyAction() {
			super("Copy member contact info to clipboard");
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
//					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		public void actionPerformed(ActionEvent e) {
			view.copyMember();
		}
	}
	
	private final class MembersRemoveAction extends AbstractAction {

	    public MembersRemoveAction() {
			super("Remove member from database...");
		}
		
		public void actionPerformed(ActionEvent e) {
			view.removeMember(view);
		}
	}
	
	private final class MembersGetMapAction extends AbstractAction {

	    public MembersGetMapAction() {
			super("Show map to selected member's address");
		}
		
		public void actionPerformed(ActionEvent e) {
			view.showMap();
		}
	}
	
	private final class MembersMenuListener implements MenuListener {

		public void menuCanceled(MenuEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void menuDeselected(MenuEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void menuSelected(MenuEvent e) {
			if (view.getSelectedMemberIndex() == null){
				membersGetMap.setEnabled(false);
			} else{
				membersGetMap.setEnabled(true);
			}
		}
	}
}
