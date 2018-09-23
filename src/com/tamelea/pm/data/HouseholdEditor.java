package com.tamelea.pm.data;

import java.awt.Window;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public final class HouseholdEditor extends FieldEditor {
	private JComboBox box;
	//HouseholdEditor adds the null item at the front
	private List<HouseholdIndex> sortedHouseholds;
	
	public HouseholdEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		sortedHouseholds = data.queryHouseholds(null, new HouseholdNameComparator(data));
		Vector<String> householdNames = new Vector<String>(
				data.listHouseholdNames(sortedHouseholds));
		householdNames.add(0, "[none]");
		sortedHouseholds.add(0, null);
		box = new JComboBox(new DefaultComboBoxModel(householdNames));
		if (initial != null) box.setSelectedIndex(
				findSortedPositionOfHouseholdIndex((HouseholdIndex)initial));
	}

	@Override
	public JComponent getComponent() {
		return box;
	}

	@Override
	public Object getValue() {
		return sortedHouseholds.get(box.getSelectedIndex()); //the null at [0] is legit value
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

	/**
	 * In the sorted list o' names, what's the position corresponding to this HouseholdIndex?
	 * @param householdIndex
	 * @return
	 */
	private int findSortedPositionOfHouseholdIndex(HouseholdIndex householdIndex) {
		if (householdIndex == null) return 0;
		for (int sortedPosition = 1; sortedPosition < sortedHouseholds.size(); ++sortedPosition) {
			if (householdIndex.equals(sortedHouseholds.get(sortedPosition)))  return sortedPosition;
		}
		throw new IllegalArgumentException("No entry for HouseholdIndex " + householdIndex);
	}

}
