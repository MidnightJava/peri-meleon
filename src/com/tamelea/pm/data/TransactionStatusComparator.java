package com.tamelea.pm.data;

import java.util.Comparator;

final class TransactionStatusComparator implements Comparator<Transaction> {
	private Data data;
	
	TransactionStatusComparator(Data data) {
		this.data = data;
	}

	public int compare(Transaction t1, Transaction t2) {
		MemberStatus s1 = (MemberStatus)data.getMemberValue(
				(MemberIndex)t1.getValue(TransactionField.INDEX), MemberField.STATUS);
		MemberStatus s2 = (MemberStatus)data.getMemberValue(
				(MemberIndex)t2.getValue(TransactionField.INDEX), MemberField.STATUS);
		return s1.compareTo(s2);
	}

}
