# ODIEM Wiki (Archived from Google Code)

> 📜 This documentation was originally hosted on [Google Code Wiki](https://code.google.com/archive/p/odiem/wikis) circa 2010-2015. It has been preserved here for historical purposes.

## Table of Contents

- [Introduction](#introduction)
- [Usage](#usage)
- [Mapping Example](#mapping-example)
- [Property Editor Example](#property-editor-example)
- [Stack Comparison](#stack-comparison)
- [Dependencies](#dependencies)

---

## Introduction

### ODIEM (ODM) - Object Directory Mapping

As well as ORMs (object relational mapping) maps java bean to SQL tables, ODIEM is a useful java library that makes you able to map a java bean to an **LDAP** object.

### Features

1. **Multi stack support** (Sun JNDI, UnboundID, Apache Directory Client API, Novell JLDAP, custom...)
2. Mapping **LDAP** object via **annotations** (using field value if no value is passed to improve usability)
3. Reusable object conversion through `java.beans.PropertyEditor`
4. Nested object support
5. Collections support
6. Easy insert, update, delete, fetch
7. Connection listener (add, remove, update, connection-close)
8. Statistics (global and stack)
9. High performance through reflection cache (less than 3% of overhead)

### Performance Benchmarks (from 2010)

From junit test `org.odiem.test.OverheadTest` (60 seconds test):

```
JNDI stack: 137286 fetch
Odiem (using JNDI stack): 133223 fetch

Odiem/JNDI > 0.97 (less than 3% of overhead)
```

UnboundID has its own persistence framework... but ODIEM was faster! 😎

```
UnboundID Persistence Framework (using UnboundID stack): 21949 fetch
Odiem (using UnboundID stack): 22530 fetch

Odiem fetched 581 entries more than UnboundID Persistence Framework (10 seconds test)
```

### Coming Soon (never implemented 😅)

- Paged Search
- Change Password
- Asynchronous Operations
- JMX support
- Schema utils (LDIF to bean, bean to LDIF)
- JAXB Integration (XSD support)

---

## Usage

First of all you have to create a connection factory:

```java
OdmConnectionFactory odmFactory = OdmDriverManager.getConnectionFactory("jndi.odm.driver");
```

Available drivers:

| Driver Name | Stack |
|-------------|-------|
| `jndi.odm.driver` | Sun JNDI Stack |
| `unboundid.odm.driver` | UnboundID Stack |
| `apache.odm.driver` | Apache Stack |
| `novell.odm.driver` | Novell Stack |
| `opends.odm.driver` | OpenDS Stack |
| `yourcustomname` | Your custom Stack |

Now you can get a connection:

```java
odmConnection = odmFactory.createConnection("localhost", 389, "cn=Directory Manager", "secret");
```

Then, if you want, you can set an event Listener:

```java
odmConnection.setConnectionListener(new MyListener());
```

Ok, that's all... try to add, update or remove a bean:

```java
OdmPerson person = new OdmPerson();
person.setCommonName("imAPersonOne");
person.setSurname("love");
person.setBasedn("ou=test,dc=cab");

odmConnection.add(person);
```

At the end, remember to close your connection:

```java
odmConnection.close();
```

---

## Mapping Example

### Example: organizationalUnit LDAP mapping

```java
@ObjectClass("organizationalUnit")
public class OdmOrganizationalUnit {

    @Attribute(value="ou", isId=true)
    private String name;
    
    @BaseDn
    private String basedn;
    
    @Child
    private OdmPerson[] persons;

    // getters and setters...
}
```

### Example: inetOrgPerson LDAP mapping

```java
@ObjectClass("inetOrgPerson")
public class OdmPerson extends OdmAbstractPerson {

    @Child
    private OdmFriend[] friends;

    // getters and setters...
}
```

### Example: organizationalPerson LDAP mapping

```java
@ObjectClass("organizationalperson")
public class OdmFriend extends OdmAbstractPerson {
    // Inherits everything from parent
}
```

### Example: person LDAP mapping (abstract base class)

```java
@ObjectClass("person")
public abstract class OdmAbstractPerson {

    @Attribute(value="cn", isId=true)
    private String commonName;
    
    @BaseDn
    private String basedn;

    @Attribute("sn")
    private String surname;

    @Attribute()
    private SimplePhone[] telephoneNumber;
    
    // getters and setters...
}
```

---

## Property Editor Example

### How to serialize a Java Object to LDAP attribute

You can use `java.beans.PropertyEditor` to convert custom objects to/from LDAP string values.

#### Example: `SimplePhoneEditor`

```java
package org.odiem.test.editors;

import java.beans.PropertyEditorSupport;
import org.odiem.test.pojo.SimplePhone;

public class SimplePhoneEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        SimplePhone simplePhone = (SimplePhone) getValue();
        if (simplePhone != null) {
            return simplePhone.getPrefix() + simplePhone.getNumber();
        } else {
            return null;
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text != null) {
            SimplePhone simplePhone = new SimplePhone(
                text.substring(0, 2), 
                text.substring(2)
            );
            setValue(simplePhone);
        }
    }
}
```

Then register the editor:

```java
PropertyEditorManager.registerEditor(SimplePhone.class, SimplePhoneEditor.class);
```

---

## Stack Comparison

### 10 second test (circa 2010)

**Machine specs:** Pentium(R) Dual-Core CPU E5700 @ 3GHz, 2GB RAM  
Server and Client on same machine.

```
odiem (using UnboundID stack): 8000 fetch
odiem (using Apache stack): 12249 fetch
odiem (using JNDI stack): 9753 fetch
odiem (using Novell stack): 8008 fetch
odiem (using OpenDS stack): 5212 fetch
```

> **Note:** Apache stack was the fastest in this test! Results may vary depending on LDAP server and network configuration.

---

## Dependencies

If you want to use ODIEM with the bundled JNDI Sun stack, you don't need other libraries.

### Apache stack

Add the following libraries to your classpath:
- `lib/apache/*.jar`

### Novell stack

Add the following libraries to your classpath:
- `lib/novell/*.jar`

### UnboundID stack

Add the following libraries to your classpath:
- `lib/unboundid/*.jar`

### OpenDS/OpenDJ stack

Add the following libraries to your classpath:
- `lib/opends/*.jar`

### Advanced features

If you want to search on a bean marked as abstract (polymorphic queries), you have to add:
- `lib/javassist.jar`
- `lib/scannotation-1.0.2.jar`

---

*This wiki content was written around 2010 and reflects the state of the library at that time. Some features or benchmarks may no longer be accurate.*

