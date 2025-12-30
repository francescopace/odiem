package org.odiem.sdk.beans;

public class OdmResult {

	private String dn;
	private long executionTime;
	private long stackExecutionTime;

	public OdmResult(String dn) {
		this.dn = dn;
	}

	public OdmResult(String dn, long executionTime, long stackExecutionTime) {
		this.dn = dn;
		this.executionTime = executionTime;
		this.stackExecutionTime = stackExecutionTime;
	}

	public String getDn() {
		return dn;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public long getStackExecutionTime() {
		return stackExecutionTime;
	}

	public void setStackExecutionTime(long stackExecutionTime) {
		this.stackExecutionTime = stackExecutionTime;
	}

}
