package com.tamelea.pm.data;
/**
 * App-specific exception from parsing XML input.
 *
 */
public class PMParseException extends Exception {
	private static final long	serialVersionUID	= 453423521068354889L;

	public PMParseException(String message) {
		super(message);
	}
}
