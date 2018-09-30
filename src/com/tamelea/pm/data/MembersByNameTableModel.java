package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.tamelea.pm.PMTable;

@SuppressWarnings("serial")
public class MembersByNameTableModel extends AbstractTableModel
implements PropertyChangeListener, PMTableModel
	{
		private static final String[] columnNames = {
			"",
			"Name",
			MemberField.SEX.displayName,
			MemberField.STATUS.displayName,
			MemberField.DATE_OF_BIRTH.displayName,
			MemberField.LAST_CHANGE.displayName,
		};
		private Data					data;
		// No longer has "[none]" prefixed
		private List<String>			filteredSortedNames;
		// No longer has null prefixed
		private List<MemberIndex>		filteredSortedIndexes;
		private Comparator<MemberIndex>	comparator;
		private MemberNameFilter			filter;
		@SuppressWarnings("unused")
		private PMTable					table;

		public MembersByNameTableModel(
				PMTable table, 
				Data data, 
				String sortFieldName, 
				MemberNameFilter filter) 
		{
			this.table = table;
			this.data = data;
			this.filter = filter;
			if (sortFieldName.equals("Name")) this.comparator = new DisplayNameComparator(data);
			else {
				MemberField sortField = MemberField.getConstantValue(sortFieldName);
				if (sortField == null) throw new IllegalArgumentException("no field for " + sortFieldName);
				this.comparator = new MemberFieldComparator(data, sortField);
			}
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
			switch (columnIndex) {
			case 0:
				return rowIndex + 1;
			case 1:
				return filteredSortedNames.get(rowIndex);
			case 2:
				return data.getMemberValue(filteredSortedIndexes.get(rowIndex), MemberField.SEX);
			case 3:
				return data.getMemberValue(filteredSortedIndexes.get(rowIndex), MemberField.STATUS);
			case 4:
				return data.getMemberValue(filteredSortedIndexes.get(rowIndex), MemberField.DATE_OF_BIRTH);
			case 5:
				return data.getMemberValue(filteredSortedIndexes.get(rowIndex), MemberField.LAST_CHANGE);
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
				return Sex.class;
			case 3:
				return MemberStatus.class;
			case 4:
				return PMDate.class;
			case 5:
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
	    	return filteredSortedIndexes.get(tableIndex);
	    }
	    
	    private void refreshData() {
	    	filteredSortedIndexes = data.queryMembers(filter, comparator);
	    	filteredSortedNames = createNameList(filteredSortedIndexes);
	    	
	     }
	    
	    private List<String> createNameList(List<MemberIndex> indexes){
	    	List<String> names = new ArrayList<String>();
//	    	indexes.remove(0);//remove expected null record
	    	for (MemberIndex index: indexes){
	    		String displayName = data.getMemberDisplayName(index);
	    		names.add(displayName);
	    	}
//	    	names.add(0, "[none]");
//	    	indexes.add(0, null);
	    	return names;
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
		
		public static String[] getSortFieldValues(){
			return columnNames;
		}
}
