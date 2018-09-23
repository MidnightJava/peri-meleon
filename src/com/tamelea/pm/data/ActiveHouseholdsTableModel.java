package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public final class ActiveHouseholdsTableModel extends AbstractTableModel 
	implements PropertyChangeListener 
{
	private static final String[] columnNames = {
		"",
		HouseholdField.NAME.displayName,
		HouseholdField.HEAD.displayName,
//		HouseholdField.ACTIVE.displayName,
		AddressField.ADDRESS.displayName,
		AddressField.ADDRESS_2.displayName,
		AddressField.CITY.displayName,
		AddressField.STATE.displayName,
		AddressField.POSTAL_CODE.displayName,
		AddressField.COUNTRY.displayName,
		AddressField.EMAIL.displayName,
		AddressField.HOME_PHONE.displayName,
	};
	private Data data;
	private List<HouseholdIndex> indexesSortedByName;

	public ActiveHouseholdsTableModel(Data data) {
		this.data = data;
		data.addPropertyChangeListener(this);
		refreshData();
	}

	public int getRowCount() {
		return indexesSortedByName.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		HouseholdIndex hi = indexesSortedByName.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return rowIndex + 1;
		case 1:
			return data.getHouseholdValue(hi, HouseholdField.NAME);
		case 2:
			MemberIndex head = (MemberIndex)data.getHouseholdValue(hi, HouseholdField.HEAD);
			return data.getMemberDisplayName(head);
		case 3:
			return data.getHouseholdValue(hi, AddressField.ADDRESS);
		case 4:
			return data.getHouseholdValue(hi, AddressField.ADDRESS_2);
		case 5:
			return data.getHouseholdValue(hi, AddressField.CITY);
		case 6:
			return data.getHouseholdValue(hi, AddressField.STATE);
		case 7:
			return data.getHouseholdValue(hi, AddressField.POSTAL_CODE);
		case 8:
			return data.getHouseholdValue(hi, AddressField.COUNTRY);
		case 9:
			return data.getHouseholdValue(hi, AddressField.EMAIL);
		case 10:
			return data.getHouseholdValueLenient(hi, AddressField.HOME_PHONE);
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
			return Integer.class;
		case 1:
			return PMString.class;
		case 2:
			return String.class;
		case 3:
			return PMString.class;
		case 4:
			return PMString.class;
		case 5:
			return PMString.class;
		case 6:
			return PMString.class;
		case 7:
			return PMString.class;
		case 8:
			return PMString.class;
		case 9:
			return PMString.class;
		case 10:
			return Phone.class;
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ index);
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
    public void setValueAt(Object value, int row, int column) {
    }
    
    public HouseholdIndex getHouseholdIndex(int tableIndex) {
    	return indexesSortedByName.get(tableIndex);
    }
    
    private void refreshData() {
    	indexesSortedByName = data.queryHouseholds(
    			new ActiveHouseholdFilter(data), 
    			new HouseholdNameComparator(data));
     }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if (e.getPropertyName().equals(Data.HOUSEHOLD_ADDED)
    			|| e.getPropertyName().equals(Data.HOUSEHOLD_CHANGED)
    			|| e.getPropertyName().equals(Data.HOUSEHOLD_REMOVED)
    			|| e.getPropertyName().equals(Data.MEMBER_ADDED)
    			|| e.getPropertyName().equals(Data.MEMBER_REMOVED)
    			|| e.getPropertyName().equals(Data.MEMBERS_CHANGED)) {
    		refreshData();
    		this.fireTableStructureChanged();
    	}
    }
}
