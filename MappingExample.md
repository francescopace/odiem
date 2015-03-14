# How to map an ldap structure on a bean structure #

## Example organizationalUnit ldap mapping ##

```

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

```

## Example inetOrgPerson ldap mapping ##

```

@ObjectClass("inetOrgPerson")
public class OdmPerson extends OdmAbstractPerson {

	@Child
	private OdmFriend[] friends;

	public void setFriends(OdmFriend... friends) {
		this.friends = friends;
	}

	public OdmFriend[] getFriends() {
		return friends;
	}

}
```

## Example organizationalperson ldap mapping ##

```
@ObjectClass("organizationalperson")
public class OdmFriend extends OdmAbstractPerson {

}
```

## Example person ldap mapping ##

```
@XmlRootElement
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
```