package org.odiem.sdk.exceptions;

public class OdmParsingException extends OdmException {

	private static final long serialVersionUID = -2681023558349876118L;

	public OdmParsingException(Class<?> pojoClass,String message) {
		super("Error parsing class "+pojoClass.getName()+": "+message);
	}
	
}
