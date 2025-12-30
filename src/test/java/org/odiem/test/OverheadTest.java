package org.odiem.test;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odiem.OdmDriverManager;
import org.odiem.sdk.OdmConnection;
import org.odiem.sdk.OdmConnectionFactory;
import org.odiem.sdk.beans.OdmChildScope;
import org.odiem.test.pojo.OdmFriend;
import org.odiem.test.pojo.OdmOrganizationalUnit;
import org.odiem.test.pojo.OdmPerson;
import org.odiem.test.pojo.SimplePhone;

/**
 * Original overhead benchmark test from 2010-2014.
 * 
 * Compares raw JNDI vs ODIEM performance to measure ODM overhead.
 * Now uses OpenLDAP via Testcontainers - no external server required!
 * 
 * Original results (2010):
 *   JNDI stack: 137,286 fetch
 *   Odiem (using JNDI stack): 133,223 fetch
 *   Odiem/JNDI > 0.97 (less than 3% overhead)
 * 
 * Run with: mvn test -Dtest=OverheadTest
 */
public class OverheadTest extends AbstractDockerLdapTest {

	private static final long TIME = 10000;

	private static InitialLdapContext ldapContext;
	private static OdmConnection odmConnection;

	private static OdmOrganizationalUnit organizationalUnit;

	private static boolean stop = false;

	@BeforeClass
	public static void setUp() throws Exception {
		// Start Docker container (from parent class)
		AbstractDockerLdapTest.startDockerLdap();

		// Raw JNDI connection
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://" + HOST + ":" + PORT);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ADMIN_DN);
		env.put(Context.SECURITY_CREDENTIALS, ADMIN_PASSWORD);
		ldapContext = new InitialLdapContext(env, null);

		// ODIEM connection (using UnboundID - more reliable than JNDI stack)
		OdmConnectionFactory odmFactory = OdmDriverManager
				.getConnectionFactory("unboundid.odm.driver", null);
		odmConnection = odmFactory.createConnection(HOST, PORT, ADMIN_DN, ADMIN_PASSWORD, false);

		// Prepare test data
		organizationalUnit = new OdmOrganizationalUnit();
		organizationalUnit.setName("test");
		organizationalUnit.setBasedn(BASE_DN);

		OdmPerson person = new OdmPerson();
		person.setCommonName("imAPersonOne");
		person.setSurname("peace");
		person.setTelephoneNumber(new SimplePhone("06", "111"), new SimplePhone("02", "222"));
		person.setBasedn("ou=test," + BASE_DN);

		organizationalUnit.setPersons(person);

		odmConnection.add(organizationalUnit);
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		if (ldapContext != null) {
			ldapContext.close();
		}
		if (odmConnection != null) {
			try {
				OdmPerson person = new OdmPerson();
				person.setCommonName("imAPersonOne");
				person.setBasedn("ou=test," + BASE_DN);
				odmConnection.remove(person);
				odmConnection.remove(organizationalUnit);
			} catch (Exception e) {
				// Ignore cleanup errors
			}
			odmConnection.close();
		}

		AbstractDockerLdapTest.stopDockerLdap();
	}

	@Test
	public void rawJndi() throws Exception {
		int n = 0;
		Timer.go();
		while (!stop) {
			ldapContext.getAttributes("cn=imAPersonOne,ou=test," + BASE_DN);
			n++;
		}
		System.out.println("Raw JNDI: " + n + " fetch");
	}

	@Test
	public void odiemWithUnboundid() throws Exception {
		OdmFriend pojo = new OdmFriend();
		pojo.setCommonName("imAPersonOne");
		pojo.setBasedn("ou=test," + BASE_DN);

		int n = 0;
		Timer.go();
		while (!stop) {
			odmConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
			n++;
		}
		System.out.println("ODIEM (UnboundID): " + n + " fetch");
	}

	static class Timer extends Thread {

		public static void go() {
			stop = false;
			new Timer().start();
		}

		@Override
		public void run() {
			try {
				sleep(TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stop = true;
		}
	}
}
