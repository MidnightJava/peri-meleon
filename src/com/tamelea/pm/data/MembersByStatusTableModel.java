package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public final class MembersByStatusTableModel extends AbstractTableModel 
implements PropertyChangeListener
{
	private static final String[] columnNames = {
		"",
		"Name",
		TransactionField.DATE.displayName,
		TransactionField.TYPE.displayName,
		TransactionField.AUTHORITY.displayName,
		TransactionField.CHURCH.displayName,
		TransactionField.COMMENT.displayName,
		MemberField.LAST_CHANGE.displayName,
	};
	private Data data;
	private List<String> filteredSortedNames;
	private List<MemberIndex> filteredIndexesSortedByName;
	private MemberStatus status;
	private ResidenceSelector rs;
	private String nameSearch;

	public MembersByStatusTableModel(Data data, MemberStatus status, ResidenceSelector rs, String nameSearch) {
		this.data = data;
		this.status = status;
		this.rs = rs;
		this.nameSearch = nameSearch;
		refreshData();
		data.addPropertyChangeListener(this);
	}

	public int getRowCount() {
		return filteredSortedNames.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Transaction latest = data.getMostRecentTransaction(filteredIndexesSortedByName.get(rowIndex));
		switch (columnIndex) {
		case 0:
			return rowIndex + 1;
		case 1:
			return filteredSortedNames.get(rowIndex);
		case 2:
			return (latest == null) ? null : latest.getValue(TransactionField.DATE);
		case 3:
			return (latest == null) ? null : latest.getValue(TransactionField.TYPE);
		case 4:
			return (latest == null) ? null : latest.getValue(TransactionField.AUTHORITY);
		case 5:
			return (latest == null) ? null : latest.getValue(TransactionField.CHURCH);
		case 6:
			return (latest == null) ? null : latest.getValue(TransactionField.COMMENT);
		case 7:
			return data.getMemberValue(filteredIndexesSortedByName.get(rowIndex), MemberField.LAST_CHANGE);
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
			return String.class;
		case 2:
			return PMDate.class;
		case 3:
			return TransactionType.class;
		case 4:
			return PMString.class;
		case 5:
			return PMString.class;
		case 6:
			return PMString.class;
		case 7:
			return PMDate.class;
		default:
			throw new IllegalArgumentException ("col index out of range " 
					+ index);
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
    
    public MemberIndex getMemberIndex(int tableIndex) {
    	return filteredIndexesSortedByName.get(tableIndex);
    }
    
    private void refreshData() {
    	filteredIndexesSortedByName = data.queryMembers(
    			new MemberStatusFilter(data, status, rs, nameSearch), 
    			new DisplayNameComparator(data));
    	filteredSortedNames = data.listDisplayNames(filteredIndexesSortedByName);
//    	HashMap<String,List<? extends Object>> namesAndIndexes = data.getMembersByStatus(status, rs);
//    	filteredSortedNames = (List<String>) namesAndIndexes.get("names");
//    	filteredIndexesSortedByName = (List<MemberIndex>) namesAndIndexes.get("members");
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
	
	public String getTSVText(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getRowCount(); i++){
			//omit the counting column
			for (int j = 1; j < getColumnCount(); j++){
				Object value = getValueAt(i,j);
				String cellText = (value == null) ? "" : (String)value.toString();
				if ( cellText.indexOf(Character.valueOf('\t').charValue()) >= 0){
					cellText = cellText.replaceAll("\t"," "); 
				} 
				cellText = cellText + "\t";
				sb.append(cellText);
			}
			sb.deleteCharAt(sb.lastIndexOf("\t"));
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
	public static String[] getSortFieldValues(){
		return columnNames;
	}
}
