package org.odiem.test.pojo;

public class SimplePhone {

	private String prefix;
	private String number;

	public SimplePhone(String prefix, String number) {
		this.prefix = prefix;
		this.number = number;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

}
