package com.tamelea.pm;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import com.tamelea.pm.data.ActiveSelector;
import com.tamelea.pm.data.Data;
/**
 * The Households menu.
 *
 */
@SuppressWarnings("serial")
final class HouseholdsEdit {
	private PeriMeleonView				view;
	private Data					data;
	private JMenu					householdsMenu;
	private HouseholdsViewAction	householdsViewAction;
	private HouseholdsView			householdsView;
	private HouseholdsNewAction		householdsNew;
	private HouseholdsEditAction	householdsEdit;
	private HouseholdsRemoveAction	householdsRemove;
	
	HouseholdsEdit(PeriMeleonView view, Data data) {
		this.view = view;
		this.data = data;
		householdsView = null;
	}
	
	public JMenu createMenu() {
		householdsMenu = new JMenu("Households");
		
		householdsViewAction = new HouseholdsViewAction();
		householdsMenu.add(householdsViewAction);
		householdsNew = new HouseholdsNewAction();
		householdsMenu.add(householdsNew);
		householdsEdit = new HouseholdsEditAction();
		householdsMenu.add(householdsEdit);
		householdsRemove = new HouseholdsRemoveAction();
		householdsMenu.add(householdsRemove);
		householdsMenu.add(view.new HouseholdsExportAction(ActiveSelector.ACTIVE));
		return householdsMenu;
	}
	
	private final class HouseholdsViewAction extends AbstractAction {

	    public HouseholdsViewAction() {
			super("View households...");
		}
		
		public void actionPerformed(ActionEvent e) {
			if (householdsView == null) {
				householdsView = new HouseholdsView(data, view);
				householdsView.sizeAndPlace();
			}
			householdsView.setVisible(true);
		}
	}
	
	private final class HouseholdsNewAction extends AbstractAction {

	    public HouseholdsNewAction() {
			super("New household...");
		}
		
		public void actionPerformed(ActionEvent e) {
			new AddHouseholdDialog(view, data).setVisible(true);
		}
	}
	
	private final class HouseholdsEditAction extends AbstractAction {

	    public HouseholdsEditAction() {
			super("Edit household...");
		}
		
		public void actionPerformed(ActionEvent e) {
			view.editHousehold(view);
		}
	}
	
	private final class HouseholdsRemoveAction extends AbstractAction {

	    public HouseholdsRemoveAction() {
			super("Remove household...");
		}
		
		public void actionPerformed(ActionEvent e) {
			view.removeHousehold(view);
		}
	}
}
