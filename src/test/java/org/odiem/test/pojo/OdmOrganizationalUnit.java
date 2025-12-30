package org.odiem.test.pojo;

import org.odiem.sdk.annotations.Attribute;
import org.odiem.sdk.annotations.BaseDn;
import org.odiem.sdk.annotations.Child;
import org.odiem.sdk.annotations.ObjectClass;

@ObjectClass("organizationalUnit")
public class OdmOrganizationalUnit {

	@Attribute(value="ou",isId=true)
	private String name;
	
	@BaseDn
	private String basedn;
	
	@Child
	private OdmPerson[] persons;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPersons(OdmPerson... persons) {
		this.persons = persons;
	}

	public OdmPerson[] getPersons() {
		return persons;
	}

	public void setBasedn(String basedn) {
		this.basedn = basedn;
	}

	public String getBasedn() {
		return basedn;
	}

}
