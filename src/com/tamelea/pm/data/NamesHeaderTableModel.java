package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * TableModel for a column of display names, used for table header.
 *
 */
@SuppressWarnings("serial")
public final class NamesHeaderTableModel extends AbstractTableModel 
	implements PropertyChangeListener 
{
	private static final String[] columnNames = {
		"Name",
	};
	private Data data;
	private List<String> sortedNames;
	private List<MemberIndex> indexesSortedByName;

	public NamesHeaderTableModel(Data data) {
		this.data = data;
		data.addPropertyChangeListener(this);
		refreshData();
	}

	public int getRowCount() {
		return sortedNames.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return sortedNames.get(rowIndex);
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ columnIndex);
		}
	}
	
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	public Class<?> getColumnClass(int index) {
		switch (index) {
		case 0:
			return String.class;
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ index);
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
    
    public MemberIndex getMemberIndex(int tableIndex) {
    	return indexesSortedByName.get(tableIndex);
    }
    
    private void refreshData() {
     	indexesSortedByName = data.queryMembers(null, new DisplayNameComparator(data));
       	sortedNames = data.listDisplayNames(indexesSortedByName);
     }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if (e.getPropertyName().equals(Data.MEMBER_ADDED)
    			|| e.getPropertyName().equals(Data.MEMBER_REMOVED)
    			|| e.getPropertyName().equals(Data.MEMBERS_CHANGED)) {
    		refreshData();
    		this.fireTableStructureChanged();
    	}
    }
}
