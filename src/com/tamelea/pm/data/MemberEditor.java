package com.tamelea.pm.data;

import java.awt.Window;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public class MemberEditor extends FieldEditor {
	private JComboBox box;
	/**
	 * MemberEditor now takes care of providing the "[none]" choice itself,
	 * as it should have all along, rather than goofing up the sorted lists
	 * for the rest of the code.
	 */
	private List<MemberIndex> sortedMembers;
	
	
	public MemberEditor(Window parent, Data data, Object initial) {
		super(parent, data, initial);
		sortedMembers = data.queryMembers(getSexFilter(), new DisplayNameComparator(data));
		Vector<String> memberNames = new Vector<String>(
				data.listDisplayNames(sortedMembers));
		memberNames.add(0, "[none]");
		sortedMembers.add(0, null);
		box = new JComboBox(new DefaultComboBoxModel(memberNames));
		if (initial != null) box.setSelectedIndex(
				findSortedPositionOfMemberIndex((MemberIndex)initial));
	}

	@Override
	public JComponent getComponent() {
		return box;
	}

	@Override
	public Object getValue() {
		return sortedMembers.get(box.getSelectedIndex()); //the null at [0] is legit value
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

	/**
	 * In the sorted list o' names, what's the position corresponding to this MemberIndex?
	 * @param memberIndex
	 * @return
	 */
	private int findSortedPositionOfMemberIndex(MemberIndex memberIndex) {
		if (memberIndex == null) return 0;
		for (int sortedPosition = 1; sortedPosition < sortedMembers.size(); ++sortedPosition) {
			if (memberIndex.equals(sortedMembers.get(sortedPosition)))  return sortedPosition;
		}
		throw new IllegalArgumentException("No entry for MemberIndex " + memberIndex);
	}
	
	protected MemberFilter getSexFilter(){
		return null;
	}
}
