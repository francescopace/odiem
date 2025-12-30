package org.odiem.test.pojo;

import org.odiem.sdk.annotations.Attribute;
import org.odiem.sdk.annotations.BaseDn;
import org.odiem.sdk.annotations.ObjectClass;

@ObjectClass("person")
public abstract class OdmAbstractPerson {

	@Attribute(value="cn",isId=true)
	private String commonName;
	
	@BaseDn
	private String basedn;

	@Attribute("sn")
	private String surname;

	@Attribute()
	private SimplePhone[] telephoneNumber;
	
	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public SimplePhone[] getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(SimplePhone... telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public void setBasedn(String basedn) {
		this.basedn = basedn;
	}

	public String getBasedn() {
		return basedn;
	}

}
