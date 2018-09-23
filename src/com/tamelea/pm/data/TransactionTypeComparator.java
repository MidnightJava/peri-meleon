package com.tamelea.pm.data;

import java.util.Comparator;

final class TransactionTypeComparator implements Comparator<Transaction> {

	public int compare(Transaction t1, Transaction t2) {
		TransactionType d1 = (TransactionType)t1.getValue(TransactionField.TYPE);
		TransactionType d2 = (TransactionType)t2.getValue(TransactionField.TYPE);
		return d1.compareTo(d2);
	}

}
