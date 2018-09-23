package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class BaptismsTableModel extends AbstractTableModel
implements PropertyChangeListener
	{
		private static final String[] columnNames = {
			"",
			"Date",
			"Name",
		};
		private Data						data;
		private PMDate						earliest, latest;
		private List<BaptismQueryRecord>	records;

		public BaptismsTableModel(
				Data data, 
				PMDate earliest,
				PMDate latest) 
		{
			this.data = data;
			this.earliest = earliest;
			this.latest = latest;
			refreshData();
			data.addPropertyChangeListener(this);
		}

		public int getRowCount() {
			return records.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return rowIndex + 1;
			case 1:
				return records.get(rowIndex).date;
			case 2:
				return records.get(rowIndex).displayName;
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
				return PMDate.class;
			case 2:
				return String.class;
			default:
				throw new IllegalArgumentException ("col index out of range " 
						+ index);
			}
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	    
//	    public MemberIndex getMemberIndex(int tableIndex) {
//	    	return filteredSortedIndexes.get(tableIndex + 1);
//	    }
	    
	    private void refreshData() {
	    	records = data.queryBaptisms(earliest, latest);
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
					String cellText = (String)getValueAt(i,j).toString();
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
}
