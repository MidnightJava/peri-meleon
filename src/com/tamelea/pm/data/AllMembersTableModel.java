package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * TableModel for all Members.
 * All visible fields are represented.
 *
 */
@SuppressWarnings("serial")
public final class AllMembersTableModel extends AbstractTableModel
	implements PropertyChangeListener
{
	static private ArrayList<MemberField> visibleFields;
	static {
		visibleFields = new ArrayList<MemberField>();
		for (MemberField field : MemberField.values()) {
			if (field.editable) visibleFields.add(field);
		}
	}
	private Data data;
	private List<MemberIndex> indexesSortedByName;
	
	public AllMembersTableModel(Data data) {
		this.data = data;
		refreshData();
		data.addPropertyChangeListener(this);
	}

	public int getRowCount() {
		return indexesSortedByName.size();
	}

	public int getColumnCount() {
		return visibleFields.size();
	}
	
	public String getColumnName(int index) {
		return visibleFields.get(index).displayName;
	}
	
	public Class<?> getColumnClass(int index) {
		return visibleFields.get(index).fieldClass;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.getMemberValue(indexesSortedByName.get(rowIndex), visibleFields.get(columnIndex));
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
    
    public MemberIndex getMemberIndex(int tableIndex) {
    	return indexesSortedByName.get(tableIndex);
    }
    
    private void refreshData() {
    	indexesSortedByName = data.queryMembers(null, new DisplayNameComparator(data));
    }
    
    public void removeListener() {
    	data.removePropertyChangeListener(this);
    }

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Data.MEMBERS_CHANGED)
				|| evt.getPropertyName().equals(Data.MEMBER_ADDED)
				|| evt.getPropertyName().equals(Data.MEMBER_REMOVED)) {
    		refreshData();
    		this.fireTableStructureChanged();
		}
		
	}

}
