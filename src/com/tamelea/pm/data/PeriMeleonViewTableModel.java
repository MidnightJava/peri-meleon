package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public final class PeriMeleonViewTableModel extends AbstractTableModel 
	implements PropertyChangeListener, PMTableModel 
{
	private static final String[] columnNames = {
		"Name",
		MemberField.EMAIL.displayName,
		AddressField.HOME_PHONE.displayName,
		MemberField.MOBILE_PHONE.displayName,
		MemberField.WORK_PHONE.displayName,
		AddressField.ADDRESS.displayName,
		AddressField.ADDRESS_2.displayName,
		AddressField.CITY.displayName,
		AddressField.STATE.displayName,
		AddressField.POSTAL_CODE.displayName,
		AddressField.COUNTRY.displayName,
	};
	private Data data;
	//These no longer are prefixed by "[none]" and null.
	private List<String> sortedNames;
	private List<MemberIndex> indexesSortedByName;

	public PeriMeleonViewTableModel(Data data) {
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
		HouseholdIndex hi = (HouseholdIndex)data.getMemberValue(
				indexesSortedByName.get(rowIndex), MemberField.HOUSEHOLD);
		switch (columnIndex) {
		case 0:
			return sortedNames.get(rowIndex);
		case 1:
			return data.getMemberValue(indexesSortedByName.get(rowIndex), MemberField.EMAIL);
		case 2:
			return data.getHouseholdValueLenient(hi, AddressField.HOME_PHONE);
		case 3:
			return data.getMemberValue(indexesSortedByName.get(rowIndex), MemberField.MOBILE_PHONE);
		case 4:
			return data.getMemberValue(indexesSortedByName.get(rowIndex), MemberField.WORK_PHONE);
		case 5:
			return data.getHouseholdValueLenient(hi, AddressField.ADDRESS);
		case 6:
			return data.getHouseholdValueLenient(hi, AddressField.ADDRESS_2);
		case 7:
			return data.getHouseholdValueLenient(hi, AddressField.CITY);
		case 8:
			return data.getHouseholdValueLenient(hi, AddressField.STATE);
		case 9:
			return data.getHouseholdValueLenient(hi, AddressField.POSTAL_CODE);
		case 10:
			return data.getHouseholdValueLenient(hi, AddressField.COUNTRY);
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
		case 1:
			return PMString.class;
		case 2:
			return Phone.class;
		case 3:
			return Phone.class;
		case 4:
			return Phone.class;
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
			return PMString.class;
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
    	indexesSortedByName = data.queryMembers(
    			new ActiveMemberFilter(data), 
    			new DisplayNameComparator(data));
    	sortedNames = data.listDisplayNames(indexesSortedByName);
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

	public String getTSVText() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeListener() {
		// TODO Auto-generated method stub
		
	}
}
