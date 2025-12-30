package org.odiem.test;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odiem.test.editors.SimplePhoneEditor;
import org.odiem.test.pojo.SimplePhone;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.beans.PropertyEditorManager;
import java.util.List;

import static org.junit.Assume.assumeTrue;

/**
 * Benchmark comparison: ODIEM vs Spring LDAP
 * 
 * Same methodology: fetch operations in 10 seconds.
 * 
 * Run with: mvn test -Dtest=SpringLdapBenchmarkTest
 * 
 * Requires Docker.
 */
public class SpringLdapBenchmarkTest {

    private static final int TEST_DURATION_SECONDS = 10;
    
    private static GenericContainer<?> ldapContainer;
    private static String host;
    private static int port;
    private static final String ADMIN_DN = "cn=admin,dc=example,dc=org";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String BASE_DN = "dc=example,dc=org";

    @BeforeClass
    public static void startLdapServer() throws Exception {
        // Register property editor for ODIEM
        PropertyEditorManager.registerEditor(SimplePhone.class, SimplePhoneEditor.class);
        
        try {
            ldapContainer = new GenericContainer<>(DockerImageName.parse("osixia/openldap:1.5.0"))
                .withExposedPorts(389)
                .withEnv("LDAP_ORGANISATION", "Example Inc")
                .withEnv("LDAP_DOMAIN", "example.org")
                .withEnv("LDAP_ADMIN_PASSWORD", "admin");
            
            ldapContainer.start();
            host = ldapContainer.getHost();
            port = ldapContainer.getMappedPort(389);
            
            Thread.sleep(2000);
            System.out.println("OpenLDAP started on " + host + ":" + port);
        } catch (Exception e) {
            System.err.println("Docker not available: " + e.getMessage());
        }
    }

    @org.junit.AfterClass
    public static void stopLdapServer() {
        if (ldapContainer != null && ldapContainer.isRunning()) {
            ldapContainer.stop();
        }
    }

    @Test
    public void compareOdiemVsSpringLdap() throws Exception {
        assumeTrue("Docker not available", host != null);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ODIEM vs Spring LDAP Benchmark");
        System.out.println("=".repeat(60));
        System.out.println("Test: fetch operations in " + TEST_DURATION_SECONDS + " seconds");
        System.out.println("Server: OpenLDAP via Docker");
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("=".repeat(60));

        // Setup Spring LDAP
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://" + host + ":" + port);
        contextSource.setUserDn(ADMIN_DN);
        contextSource.setPassword(ADMIN_PASSWORD);
        contextSource.setBase(BASE_DN);
        contextSource.afterPropertiesSet();
        
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        // Create test OU and person using Spring LDAP
        createTestData(ldapTemplate);

        System.out.println("\n--- Results ---\n");

        // Benchmark Spring LDAP
        int springCount = benchmarkSpringLdap(ldapTemplate);
        double springOpsPerSec = springCount / (double) TEST_DURATION_SECONDS;
        System.out.printf("Spring LDAP:     %,6d fetch/%ds (%,.0f ops/sec)%n", 
            springCount, TEST_DURATION_SECONDS, springOpsPerSec);

        // Benchmark ODIEM
        int odiemCount = benchmarkOdiem();
        double odiemOpsPerSec = odiemCount / (double) TEST_DURATION_SECONDS;
        System.out.printf("ODIEM:           %,6d fetch/%ds (%,.0f ops/sec)%n", 
            odiemCount, TEST_DURATION_SECONDS, odiemOpsPerSec);

        // Comparison
        System.out.println("\n--- Comparison ---\n");
        if (odiemOpsPerSec > springOpsPerSec) {
            System.out.printf("ODIEM is %.1fx faster than Spring LDAP%n", 
                odiemOpsPerSec / springOpsPerSec);
        } else {
            System.out.printf("Spring LDAP is %.1fx faster than ODIEM%n", 
                springOpsPerSec / odiemOpsPerSec);
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    private void createTestData(LdapTemplate ldapTemplate) {
        // Create OU
        Attributes ouAttrs = new BasicAttributes();
        ouAttrs.put("objectClass", "organizationalUnit");
        ouAttrs.put("ou", "benchmark");
        try {
            ldapTemplate.bind("ou=benchmark", null, ouAttrs);
        } catch (Exception e) {
            // May already exist
        }

        // Create person
        Attributes personAttrs = new BasicAttributes();
        BasicAttribute ocAttr = new BasicAttribute("objectClass");
        ocAttr.add("top");
        ocAttr.add("person");
        ocAttr.add("organizationalPerson");
        ocAttr.add("inetOrgPerson");
        personAttrs.put(ocAttr);
        personAttrs.put("cn", "TestUser");
        personAttrs.put("sn", "Benchmark");
        try {
            ldapTemplate.bind("cn=TestUser,ou=benchmark", null, personAttrs);
        } catch (Exception e) {
            // May already exist
        }
    }

    private int benchmarkSpringLdap(LdapTemplate ldapTemplate) {
        // Warmup
        for (int i = 0; i < 100; i++) {
            ldapTemplate.search(
                LdapQueryBuilder.query()
                    .base("ou=benchmark")
                    .where("cn").is("TestUser"),
                new PersonAttributesMapper()
            );
        }

        // Benchmark
        long endTime = System.currentTimeMillis() + (TEST_DURATION_SECONDS * 1000);
        int count = 0;
        
        while (System.currentTimeMillis() < endTime) {
            List<Person> results = ldapTemplate.search(
                LdapQueryBuilder.query()
                    .base("ou=benchmark")
                    .where("cn").is("TestUser"),
                new PersonAttributesMapper()
            );
            count++;
        }

        return count;
    }

    private int benchmarkOdiem() throws Exception {
        org.odiem.sdk.OdmConnectionFactory factory = 
            org.odiem.OdmDriverManager.getConnectionFactory("unboundid.odm.driver");
        org.odiem.sdk.OdmConnection connection = 
            factory.createConnection(host, port, ADMIN_DN, ADMIN_PASSWORD);

        // Warmup
        for (int i = 0; i < 100; i++) {
            org.odiem.test.pojo.OdmPerson p = new org.odiem.test.pojo.OdmPerson();
            p.setCommonName("TestUser");
            p.setBasedn("ou=benchmark," + BASE_DN);
            connection.fetch(p, org.odiem.sdk.beans.OdmChildScope.NO_CHILDS);
        }

        // Benchmark
        long endTime = System.currentTimeMillis() + (TEST_DURATION_SECONDS * 1000);
        int count = 0;
        
        while (System.currentTimeMillis() < endTime) {
            org.odiem.test.pojo.OdmPerson p = new org.odiem.test.pojo.OdmPerson();
            p.setCommonName("TestUser");
            p.setBasedn("ou=benchmark," + BASE_DN);
            connection.fetch(p, org.odiem.sdk.beans.OdmChildScope.NO_CHILDS);
            count++;
        }

        return count;
    }

    // Simple POJO for Spring LDAP
    public static class Person {
        private String cn;
        private String sn;

        public String getCn() { return cn; }
        public void setCn(String cn) { this.cn = cn; }
        public String getSn() { return sn; }
        public void setSn(String sn) { this.sn = sn; }
    }

    // AttributesMapper for Spring LDAP
    private static class PersonAttributesMapper implements AttributesMapper<Person> {
        @Override
        public Person mapFromAttributes(Attributes attrs) throws NamingException {
            Person person = new Person();
            person.setCn((String) attrs.get("cn").get());
            person.setSn((String) attrs.get("sn").get());
            return person;
        }
    }
}

