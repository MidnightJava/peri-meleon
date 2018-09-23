package com.tamelea.pm.data;

import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public final class HouseholdCandidatesTableModel extends AbstractTableModel {
	//no property change listener because the underlying data don't change during lifetime
	private static final String[] columnNames = {
		"",
		"Member",
	};
	private Data data;
	private HouseholdIndex householdIndex;
	private List<MemberIndex> members;
	
	public HouseholdCandidatesTableModel(Data data, HouseholdIndex householdIndex) {
		this.data = data;
		this.householdIndex = householdIndex;
		members = data.getHouseholdCandidates(householdIndex);
	}

	public int getRowCount() {
		return members.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}
	
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	public Class<?> getColumnClass(int index) {
		switch (index) {
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ index);
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return data.containsAsOther(householdIndex, members.get(rowIndex));
		case 1:
			return data.makeDisplayName(members.get(rowIndex));
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ columnIndex);
		}
	}

	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}
	
    public void setValueAt(Object value, int row, int column) {
		switch (column) {
		case 0:
	    	data.setOtherMembership(householdIndex, members.get(row), (Boolean)value);
	    	break;
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ column);
		}
    }

}
