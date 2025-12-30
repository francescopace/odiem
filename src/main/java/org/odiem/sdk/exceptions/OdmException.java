package org.odiem.sdk.exceptions;

public class OdmException extends Exception {

	private static final long serialVersionUID = -7089726442509496668L;

	public OdmException(String message) {
		super(message);
	}
	
	public OdmException(Exception exception) {
		super(exception);
	}

}
