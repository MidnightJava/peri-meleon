package com.tamelea.pm.data;

import javax.swing.table.TableModel;

public interface PMTableModel extends TableModel {

	 public MemberIndex getMemberIndex(int tableIndex);
	 
	 public void removeListener();
	 
	 public String getTSVText();
}
