package com.tamelea.pm.data;

final class TransactionDateFilter implements TransactionFilter {
	private PMDate startDate, endDate;
	
	TransactionDateFilter(PMDate startDate, PMDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean match(Transaction transaction) {
		PMDate date = (PMDate)transaction.getValue(TransactionField.DATE);
		return startDate.compareTo(date) <= 0 && endDate.compareTo(date) >= 0;
	}

}
