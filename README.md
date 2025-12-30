# ODIEM - Object Directory Mapping for LDAP

**ODIEM** (Object Directory Mapping) is a Java library that provides ORM-like capabilities for LDAP directories. It allows you to map LDAP entries to Java POJOs using annotations, similar to how JPA/Hibernate works for relational databases.

---

## Why ODIEM Was Created

Back in 2010, there was **no ORM for LDAP** in the Java ecosystem. While relational databases had JPA/Hibernate, developers working with LDAP had to write boilerplate code to map entries to objects manually.

ODIEM was created to fill this gap, with two main goals:

1. **Annotation-based mapping** - Similar to JPA, use `@ObjectClass`, `@Attribute`, `@BaseDn` annotations to define mappings declaratively
2. **Support all LDAP stacks** - A pluggable architecture that works with any LDAP SDK available at the time (JNDI, UnboundID, Apache Directory, OpenDJ, Novell JLDAP)

The library was written for **Java 5** (the first version with annotations!) and now has been minimally updated to compile with modern Java 8-21. Some stacks (OpenDJ, Novell) have been excluded due to API changes or unavailable dependencies.

You can consider this as a piece of "archaeo-informatics"!

> ⚠️ **Historical Project**: This library is published here for historical/educational purposes. It is not actively maintained. For modern projects, consider [Spring LDAP](https://spring.io/projects/spring-ldap).

---

## History

This project was originally hosted on **Google Code** (remember that?) before Google shut down the service in 2016. The original project page is still accessible via the Google Code Archive:

- **Original Project**: [https://code.google.com/archive/p/odiem/](https://code.google.com/archive/p/odiem/)
- **Original Wiki**: [https://code.google.com/archive/p/odiem/wikis](https://code.google.com/archive/p/odiem/wikis)
- **Original Wiki converted to Markdown**: [docs/README.md](docs/WIKI.md)

### Timeline

| Date | Event |
|------|-------|
| Nov 2010 | Initial development, first commits |
| Feb 2011 | Added OpenDS/OpenDJ stack |
| Dec 2012 | Libraries update |
| Dec 2014 | Last commit on Google Code |
| Jan 2016 | Google Code shutdown / Migrated to GitHub|
| Dec 2025 | Converted to Maven, restored for Java 8-21 |

## Features

- **Annotation-based mapping**: Use `@ObjectClass`, `@Attribute`, `@BaseDn`, and `@Child` annotations to map Java classes to LDAP entries
- **Multiple LDAP SDK support**: Pluggable architecture supporting 5 different LDAP client libraries:
  - JNDI (Java built-in)
  - UnboundID LDAP SDK
  - Apache Directory API
  - OpenDJ SDK
  - Novell JLDAP
- **Hierarchical mapping**: Automatic handling of parent-child relationships in the LDAP tree via `@Child` annotation
- **Abstract class support**: Polymorphic mapping with automatic subclass detection based on objectClass
- **Custom type conversion**: Uses Java `PropertyEditor` for custom attribute value conversion
- **Connection pooling**: Supported via driver properties
- **Proxied authorization**: Built-in support for LDAP proxy authorization
- **High performance**: Less than 3% overhead compared to raw LDAP SDK calls (see [benchmarks](docs/WIKI.md#performance-benchmarks-from-2010))

## Architecture

```
┌────────────────────────────────────────────────────────────┐
│                      Application                           │
├────────────────────────────────────────────────────────────┤
│                    OdmConnection                           │
│         (add, update, remove, fetch, search)               │
├────────────────────────────────────────────────────────────┤
│                       OdmPojo                              │
│            (annotation processing, mapping)                │
├────────────────────────────────────────────────────────────┤
│                   OdmDriverManager                         │
│              (driver discovery & factory)                  │
├──────────┬──────────┬──────────┬──────────┬────────────────┤
│   JNDI   │UnboundID │  Apache  │  OpenDJ  │    Novell      │
│  Stack   │  Stack   │  Stack   │  Stack   │    Stack       │
└──────────┴──────────┴──────────┴──────────┴────────────────┘
```

## Quick Start

### 1. Define your POJO

```java
@ObjectClass("inetOrgPerson")
public class Person {

    @Attribute(value = "cn", isId = true)
    private String commonName;

    @BaseDn
    private String baseDn;

    @Attribute("sn")
    private String surname;

    @Attribute
    private String[] telephoneNumber;

    @Child
    private Address[] addresses;

    // getters and setters...
}
```

### 2. Create a connection

```java
// Get connection factory for a specific driver
OdmConnectionFactory factory = OdmDriverManager
    .getConnectionFactory("jndi.odm.driver", properties);

// Create connection
OdmConnection connection = factory.createConnection(
    "localhost", 389, "cn=admin,dc=example,dc=com", "password");
```

### 3. CRUD Operations

```java
// Create
Person person = new Person();
person.setCommonName("John Doe");
person.setSurname("Doe");
person.setBaseDn("ou=people,dc=example,dc=com");
connection.add(person);

// Read (fetch)
Person template = new Person();
template.setCommonName("John Doe");
template.setBaseDn("ou=people,dc=example,dc=com");
connection.fetch(template, OdmChildScope.SUB);

// Update
person.setSurname("Smith");
connection.update(person);

// Search
OdmSearchResult result = connection.search(
    "ou=people,dc=example,dc=com",
    Person.class,
    OdmSearchScope.SUB,
    "sn=Smith",
    null
);

// Delete
connection.remove(person);
```

## Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@ObjectClass` | Class | Maps the class to an LDAP objectClass |
| `@Attribute` | Field | Maps the field to an LDAP attribute |
| `@BaseDn` | Field | Marks the field that holds the parent DN |
| `@Child` | Field | Marks a field that contains child entries |

## Supported LDAP Stacks

| Driver Name | Library | Status | Notes |
|-------------|---------|--------|-------|
| `jndi.odm.driver` | Java JNDI | ✅ Active | Built into JDK, no additional dependency |
| `unboundid.odm.driver` | UnboundID LDAP SDK | ✅ Active | Recommended |
| `apache.odm.driver` | Apache Directory API | ✅ Active | Uses shared-all 1.0.0-M13 |
| `opends.odm.driver` | OpenDJ SDK | ⚠️ Excluded | Dependency not found on Maven Central |
| `novell.odm.driver` | Novell JLDAP | ⚠️ Excluded | Dependency not found on Maven Central |

*Note: OpenDJ and Novell stacks are excluded from compilation. The original code was written for older API versions that are no longer available.*

## Building

```bash
# Build with tests (requires Docker)
mvn clean package
```

## Testing

The test suite uses **OpenLDAP via Testcontainers** - requires Docker, no manual server setup!

**11 tests** (original tests from 2010, updated to use Docker):

| Test | Description |
|------|-------------|
| `OdiemTest` (6 tests) | CRUD operations: add, update, fetch, search, remove, upsert |
| `StackTest` (2 tests) | Stack comparison: UnboundID vs Apache throughput |
| `OverheadTest` (2 tests) | Overhead measurement: raw JNDI vs ODIEM |
| `SpringLdapBenchmarkTest` (1 test) | Benchmark comparison with Spring LDAP |

Run the tests:
```bash
# All tests (requires Docker for LDAP Server)
mvn test
```

## Performance Benchmarks

Throughput test: **how many fetch operations in 10 seconds?**

Same methodology as the original 2010 benchmark, using a real LDAP server.

### 2010 Results (Original from old Wiki)

- **Hardware**: Pentium Dual-Core E5700 @ 3GHz, 2GB RAM
- **Java**: 6
- **Server**: Client and server on same machine

| Stack | Fetch/10s | Ops/sec |
|-------|-----------|---------|
| Apache | 12,249 | 1,225 |
| JNDI | 9,753 | 975 |
| UnboundID | 8,000 | 800 |
| Novell | 8,008 | 801 |
| OpenDS | 5,212 | 521 |

### 2025 Results (Same software, 15 years later)

- **Hardware**: Apple M1 Pro, 8 cores
- **Java**: 21.0.7
- **Server**: OpenLDAP via Docker (Testcontainers)

| Stack | Fetch/10s | Ops/sec |
|-------|-----------|---------|
| UnboundID | 31,927 | 3,193 |
| Apache | 29,618 | 2,962 |

Run with: `mvn test -Dtest=StackTest` (requires Docker)

### Comparison 2010 vs 2025 (Moore's Law in action!)

| Stack | 2010 | 2025 | Improvement |
|-------|------|------|-------------|
| UnboundID | 800 ops/s | 3,193 ops/s | **4x faster** |
| Apache | 1,225 ops/s | 2,962 ops/s | **2.4x faster** |

*UnboundID aged like fine wine - it was one of the slowest in 2010, now it's the fastest!*

### ODIEM vs Modern Spring LDAP (2025)

Same test methodology, same OpenLDAP server via Docker:

| Framework | Fetch/10s | Ops/sec |
|-----------|-----------|---------|
| **ODIEM** (2010) | 31,737 | 3,174 |
| Spring LDAP (2025) | 8,348 | 835 |

**A 15-year-old library beats the modern framework by 3.8x!**

Sometimes simplicity wins. ODIEM does one thing and does it well - no magic, no proxies, just fast mapping.
ODIEM focused on simplicity and performance. Spring LDAP offers a complete enterprise solution.

Run with: `mvn test -Dtest=SpringLdapBenchmarkTest` (requires Docker)

### Feature Comparison: ODIEM vs Spring LDAP

Spring LDAP offers many more features (transactions, pooling, repository pattern, etc.). This benchmark only measures raw fetch throughput. 
For real projects, use Spring LDAP - it's actively maintained!

| Feature | ODIEM (2010) | Spring LDAP (2025) |
|---------|--------------|-------------------|
| **Annotation-based mapping** | ✅ `@ObjectClass`, `@Attribute`, `@BaseDn`, `@Child` | ✅ `@Entry`, `@Attribute`, `@Id` |
| **Automatic attribute mapping** | ✅ Zero boilerplate | ⚠️ Requires `AttributesMapper` or ODM module |
| **CRUD operations** | ✅ `add`, `update`, `fetch`, `remove`, `search` | ✅ Via `LdapTemplate` or repositories |
| **Child entry mapping** | ✅ `@Child` annotation | ❌ Manual handling required |
| **Multiple LDAP SDK support** | ✅ 5 pluggable drivers | ❌ JNDI only |
| **Connection pooling** | ✅ Driver-level | ✅ Via `PoolingContextSource` |
| **Transactions** | ❌ | ✅ LDAP compensating transactions |
| **Spring Security integration** | ❌ | ✅ Native integration |
| **Query DSL** | ❌ Filter strings only | ✅ `LdapQueryBuilder`, QueryDSL |

*Fun fact: the `@Child` annotation for automatic hierarchical mapping was quite innovative for 2010 - and Spring LDAP still doesn't have an equivalent 15 years later!*

## Requirements

- **Java 8 or higher** (tested with Java 8, 11, 17, 21)
- Maven 3.x

## Dependencies

The core library has minimal dependencies. LDAP stack libraries are optional - include only the one you need:

```xml
<!-- For UnboundID stack -->
<dependency>
    <groupId>com.unboundid</groupId>
    <artifactId>unboundid-ldapsdk</artifactId>
    <version>6.0.11</version>
</dependency>

<!-- For Apache Directory stack -->
<dependency>
    <groupId>org.apache.directory.shared</groupId>
    <artifactId>shared-all</artifactId>
    <version>1.0.0-M13</version>
</dependency>
```


## Changes from Original (2025 Restoration)

The following changes were made to restore the project for modern Java:

| Change | File(s) | Reason |
|--------|---------|--------|
| `sun.misc.Service` → `java.util.ServiceLoader` | `OdmDriverManager.java` | `sun.misc.Service` removed in Java 9 |
| Removed `@XmlRootElement` | Test POJOs | JAXB removed from JDK 11 (was only used for debug logging) |
| Excluded OpenDJ stack | `pom.xml` | OpenDJ SDK 4.x API incompatible with original code |
| Excluded Novell stack | `pom.xml` | Dependency not available on Maven Central |
| Tests use Testcontainers | `*Test.java` | Original tests required manual LDAP server setup |

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Francesco Pace: francesco.pace@gmail.com

LinkedIn: [linkedin.com/in/francescopace](https://linkedin.com/in/francescopace)
