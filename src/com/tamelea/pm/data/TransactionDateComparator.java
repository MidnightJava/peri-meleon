package com.tamelea.pm.data;

import java.util.Comparator;

final class TransactionDateComparator implements Comparator<Transaction> {

	public int compare(Transaction t1, Transaction t2) {
		PMDate d1 = (PMDate)t1.getValue(TransactionField.DATE);
		PMDate d2 = (PMDate)t2.getValue(TransactionField.DATE);
		return d1.compareTo(d2);
	}

}
