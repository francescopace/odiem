package org.odiem.test;

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
 * Original stack comparison tests from 2010-2014.
 * 
 * Benchmarks different LDAP SDK implementations.
 * Now uses OpenLDAP via Testcontainers - no external server required!
 * 
 * Run with: mvn test -Dtest=StackTest
 */
public class StackTest extends AbstractDockerLdapTest {

	private static final long TIME = 10000;

	private static OdmConnection odmUnboundidConnection;
	private static OdmConnection odmApacheConnection;

	private static OdmOrganizationalUnit organizationalUnit;
	private static OdmFriend pojo;

	private static boolean stop = false;

	@BeforeClass
	public static void setUp() throws Exception {
		// Start Docker container (from parent class)
		AbstractDockerLdapTest.startDockerLdap();

		// odiem unboundid
		OdmConnectionFactory odmFactoryU = OdmDriverManager
				.getConnectionFactory("unboundid.odm.driver", null);
		odmUnboundidConnection = odmFactoryU.createConnection(HOST, PORT,
				ADMIN_DN, ADMIN_PASSWORD, true);

		// odiem apache
		OdmConnectionFactory odmFactoryA = OdmDriverManager
				.getConnectionFactory("apache.odm.driver", null);
		odmApacheConnection = odmFactoryA.createConnection(HOST, PORT,
				ADMIN_DN, ADMIN_PASSWORD, true);

		// Note: JNDI, Novell, and OpenDS stacks are not tested because:
		// - JNDI uses TreeDeleteControl not supported by OpenLDAP
		// - Novell and OpenDS stacks are excluded from compilation

		// Prepare test data
		organizationalUnit = new OdmOrganizationalUnit();
		organizationalUnit.setName("test");
		organizationalUnit.setBasedn(BASE_DN);

		OdmPerson person = new OdmPerson();
		person.setCommonName("imAPersonOne");
		person.setSurname("peace");
		person.setTelephoneNumber(new SimplePhone("06", "111"),
				new SimplePhone("02", "222"));
		person.setBasedn("ou=test," + BASE_DN);

		organizationalUnit.setPersons(person);

		odmUnboundidConnection.add(organizationalUnit);

		pojo = new OdmFriend();
		pojo.setCommonName("imAPersonOne");
		pojo.setBasedn("ou=test," + BASE_DN);
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		// Clean up test data
		if (odmUnboundidConnection != null) {
			try {
				OdmPerson person = new OdmPerson();
				person.setCommonName("imAPersonOne");
				person.setBasedn("ou=test," + BASE_DN);
				odmUnboundidConnection.remove(person);

				odmUnboundidConnection.remove(organizationalUnit);
			} catch (Exception e) {
				// Ignore cleanup errors
			}
			odmUnboundidConnection.close();
		}
		if (odmApacheConnection != null) {
			odmApacheConnection.close();
		}

		AbstractDockerLdapTest.stopDockerLdap();
	}

	@Test
	public void odiemUnboundid() throws Exception {
		int n = 0;
		Timer.go();
		while (!stop) {
			odmUnboundidConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
			n++;
		}
		System.out.println("odiem (UnboundID stack): " + n + " fetch");
	}

	@Test
	public void odiemApache() throws Exception {
		int n = 0;
		Timer.go();
		while (!stop) {
			odmApacheConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
			n++;
		}
		System.out.println("odiem (Apache stack): " + n + " fetch");
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
