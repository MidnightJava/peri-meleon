package com.tamelea.pm.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * TableModel for Officer service record.
 * All visible fields are represented.
 *
 */
@SuppressWarnings("serial")
public final class ServiceTableModel extends AbstractTableModel
	implements PropertyChangeListener
{
	static private ArrayList<ServiceField> visibleFields;
	static {
		visibleFields = new ArrayList<ServiceField>();
		for (ServiceField field : ServiceField.values()) {
			if (field.editable) visibleFields.add(field);
		}
	}
	private Data data;
	private MemberIndex index;
	private List<Service> services;
	
	public ServiceTableModel(Data data, MemberIndex index) {
		this.data = data;
		this.index = index;
		refreshData();
		data.addPropertyChangeListener(this);
	}

	public int getRowCount() {
		return services.size();
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
		return services.get(rowIndex).getValue(visibleFields.get(columnIndex));
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
    public void setValueAt(Object value, int row, int column) {
    	services.get(row).setValue(visibleFields.get(column), value);
    }
    
    public Service getService(int listIndex) {
    	return services.get(listIndex);
    }
    
    private void refreshData() {
		services = data.getSortedServices(index);
    }
    
    public void removeListener() {
    	data.removePropertyChangeListener(this);
    }

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Data.MEMBERS_CHANGED)) {
    		refreshData();
    		this.fireTableStructureChanged();
		}
		
	}

}
