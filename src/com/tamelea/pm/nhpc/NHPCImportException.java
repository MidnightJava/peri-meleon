package com.tamelea.pm.nhpc;

@SuppressWarnings("serial")
public final class NHPCImportException extends Exception {
	private final int lineNumber;
	
	public NHPCImportException(int lineNumber, String message, Throwable cause) {
		super(message, cause);
		this.lineNumber = lineNumber;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

}
