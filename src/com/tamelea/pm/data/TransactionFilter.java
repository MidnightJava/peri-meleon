package com.tamelea.pm.data;

public interface TransactionFilter {
	public boolean match(Transaction transaction);
}
