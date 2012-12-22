package org.odiem.sdk.beans;


public class OdmSearchResultEntry extends OdmResult {

	private OdmAttribute[] attributes;

	public OdmSearchResultEntry(String dn, OdmAttribute[] attributes) {
		super(dn);
		this.attributes=attributes;
	}

	public OdmSearchResultEntry(String dn, OdmAttribute[] attributes,
			long executionTime, long stackExecutionTime) {
		this(dn, attributes);
		setExecutionTime(executionTime);
		setStackExecutionTime(stackExecutionTime);
	}

	public OdmAttribute[] getAttributes() {
		return attributes;
	}

}
