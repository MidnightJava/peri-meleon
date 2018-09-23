package com.tamelea.pm.data;

import java.util.Comparator;

final class TransactionStatisticalComparator implements Comparator<Transaction> {
	private TransactionDateComparator	dateComparator;
	private TransactionStatusComparator	statusComparator;
	private TransactionTypeComparator	typeComparator;
	
	TransactionStatisticalComparator(Data data) {
		dateComparator = new TransactionDateComparator();
		statusComparator = new TransactionStatusComparator(data);
		typeComparator = new TransactionTypeComparator();
	}

	public int compare(Transaction t1, Transaction t2) {
		int typeInd = typeComparator.compare(t1, t2);
		if (typeInd != 0) return typeInd;
		int statusInd = statusComparator.compare(t1, t2);
		if (statusInd != 0) return statusInd;
		return dateComparator.compare(t1, t2);
	}

}
