package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public final class HouseholdsViewTableModel extends AbstractTableModel 
	implements PropertyChangeListener 
{
	private static final String[] columnNames = {
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
	//No longer assume null and "[none]" at front
	private List<String> sortedNames;
	private List<HouseholdIndex> indexesSortedByName;

	public HouseholdsViewTableModel(Data data) {
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
		HouseholdIndex hi = indexesSortedByName.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return data.getHouseholdValue(hi, HouseholdField.NAME);
		case 1:
			MemberIndex head = (MemberIndex)data.getHouseholdValue(hi, HouseholdField.HEAD);
			return data.getMemberDisplayName(head);
		case 2:
			return data.getHouseholdValue(hi, AddressField.ADDRESS);
		case 3:
			return data.getHouseholdValue(hi, AddressField.ADDRESS_2);
		case 4:
			return data.getHouseholdValue(hi, AddressField.CITY);
		case 5:
			return data.getHouseholdValue(hi, AddressField.STATE);
		case 6:
			return data.getHouseholdValue(hi, AddressField.POSTAL_CODE);
		case 7:
			return data.getHouseholdValue(hi, AddressField.COUNTRY);
		case 8:
			return data.getHouseholdValue(hi, AddressField.EMAIL);
		case 9:
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
			return PMString.class;
		case 1:
			return String.class;
//		case 2:
//			return Boolean.class;
		case 2:
			return PMString.class;
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
//		switch (column) {
//		case 1:
//	    	panel.setEscorted(candidates.get(row), (Boolean)value);
//	    	break;
//		default:
//			throw new IllegalArgumentException ("col index out of range " 
//					+ column);
//		}
    }
    
    public HouseholdIndex getHouseholdIndex(int tableIndex) {
    	return indexesSortedByName.get(tableIndex);
    }
    
    private void refreshData() {
    	indexesSortedByName = data.queryHouseholds(null, new HouseholdNameComparator(data));
    	sortedNames = data.listHouseholdNames(indexesSortedByName);
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
