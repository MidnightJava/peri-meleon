package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public final class BirthdaysTableModel extends AbstractTableModel
	implements PropertyChangeListener
{
	private static final String[] columnNames = new String[] {
		"Name",
		"Date of Birth",
	};
	private Data data;
	private Month month;
	private List<MemberIndex> indexes;
	
	public BirthdaysTableModel(Data data, Month month) {
		this.data = data;
		this.month = month;
		refreshData();
		data.addPropertyChangeListener(this);
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return indexes.size();
	}
	
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	public Class<?> getColumnClass(int index) {
		switch (index){
		case 0:
			return String.class;
		case 1:
			return PMDate.class;
		default:
			return String.class;
		}
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return data.makeInformalName(indexes.get(rowIndex));
		case 1:
			return data.getMemberValue(indexes.get(rowIndex), MemberField.DATE_OF_BIRTH);
		default:
			throw new IllegalArgumentException("column index " + columnIndex + " out of range");
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
    
    private void refreshData() {
    	indexes = data.queryMembers(
    			new BirthdayMemberFilter(data, month), 
    			new DayOfMonthComparator(data));
    }
    
    public void removeListener() {
    	data.removePropertyChangeListener(this);
    }
    
    public String toString() {
    	if (indexes.isEmpty()) return "";
    	StringBuffer result = new StringBuffer();
    	result.append(makeBirthdayEntry(0));
    	for (int index = 1; index < indexes.size(); ++index) {
    		result.append(", ");
    		result.append(makeBirthdayEntry(index));
    	}
    	return result.toString();
    }
    
    public MemberIndex getMemberIndex(int tableIndex) {
    	return indexes.get(tableIndex + 1);
    }
    
    private StringBuffer makeBirthdayEntry(int index) {
    	StringBuffer result = new StringBuffer();
    	result.append(data.getInformalFrstName(indexes.get(index)));
    	result.append(" ");
    	result.append(data.getMemberValue(indexes.get(index), MemberField.LAST_NAME));
    	result.append(" (");
    	result.append(((PMDate)data.getMemberValue(
    			indexes.get(index), MemberField.DATE_OF_BIRTH)).getDayOfMonth());
    	result.append(")");
    	return result;
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
