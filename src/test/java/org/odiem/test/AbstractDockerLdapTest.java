package org.odiem.test;

import java.beans.PropertyEditorManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.odiem.test.editors.SimplePhoneEditor;
import org.odiem.test.pojo.SimplePhone;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests using OpenLDAP via Testcontainers.
 * 
 * This replaces the need for a manually configured external LDAP server.
 */
public abstract class AbstractDockerLdapTest {

    protected static final String BASE_DN = "dc=example,dc=org";
    protected static final String ADMIN_DN = "cn=admin,dc=example,dc=org";
    protected static final String ADMIN_PASSWORD = "admin";

    protected static String HOST;
    protected static int PORT;

    private static GenericContainer<?> ldapContainer;

    @BeforeClass
    public static void startDockerLdap() throws Exception {
        // Register property editors
        PropertyEditorManager.registerEditor(SimplePhone.class, SimplePhoneEditor.class);

        // Start OpenLDAP container
        ldapContainer = new GenericContainer<>(DockerImageName.parse("osixia/openldap:1.5.0"))
            .withExposedPorts(389)
            .withEnv("LDAP_ORGANISATION", "Example Inc")
            .withEnv("LDAP_DOMAIN", "example.org")
            .withEnv("LDAP_ADMIN_PASSWORD", "admin");
        
        ldapContainer.start();
        
        HOST = ldapContainer.getHost();
        PORT = ldapContainer.getMappedPort(389);
        
        // Wait for server to be fully ready
        Thread.sleep(2000);
        
        System.out.println("OpenLDAP container started on " + HOST + ":" + PORT);
    }

    @AfterClass
    public static void stopDockerLdap() {
        if (ldapContainer != null && ldapContainer.isRunning()) {
            ldapContainer.stop();
            System.out.println("OpenLDAP container stopped");
        }
    }
}

