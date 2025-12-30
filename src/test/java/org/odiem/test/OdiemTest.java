package org.odiem.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.odiem.OdmDriverManager;
import org.odiem.sdk.OdmConnection;
import org.odiem.sdk.OdmConnectionFactory;
import org.odiem.sdk.OdmConnectionListener;
import org.odiem.sdk.beans.OdmChildScope;
import org.odiem.sdk.beans.OdmSearchResult;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.stacks.unbounid.DriverImpl;
import org.odiem.test.pojo.OdmAbstractPerson;
import org.odiem.test.pojo.OdmFriend;
import org.odiem.test.pojo.OdmOrganizationalUnit;
import org.odiem.test.pojo.OdmPerson;
import org.odiem.test.pojo.SimplePhone;

/**
 * Original integration tests from 2010-2014.
 * 
 * Now uses OpenLDAP via Testcontainers - no external server required!
 * 
 * Run with: mvn test -Dtest=OdiemTest
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OdiemTest extends AbstractDockerLdapTest implements OdmConnectionListener {

	private static OdmConnection odmConnection;

	@BeforeClass
	public static void setUp() throws Exception {
		// Start Docker container (from parent class)
		AbstractDockerLdapTest.startDockerLdap();

		System.out.println("Available drivers: " + OdmDriverManager.getDrivers());

		Properties properties = new Properties();
		properties.put(DriverImpl.PROPS.AUTO_RECONNECT, "false");
		properties.put(DriverImpl.PROPS.POOL_SIZE, "10");

		// Use UnboundID instead of JNDI (JNDI requires TreeDeleteControl)
		OdmConnectionFactory odmFactory = OdmDriverManager
				.getConnectionFactory("unboundid.odm.driver", properties);

		odmConnection = odmFactory.createConnection(HOST, PORT, ADMIN_DN, ADMIN_PASSWORD);
		odmConnection.setConnectionListener(new OdiemTest());
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		if (odmConnection != null) {
			odmConnection.close();
		}
		AbstractDockerLdapTest.stopDockerLdap();
	}

	@Test
	public void test1_Add() {
		try {
			OdmOrganizationalUnit organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn(BASE_DN);

			OdmPerson person1 = new OdmPerson();
			person1.setCommonName("imAPersonOne");
			person1.setSurname("peace");
			person1.setTelephoneNumber(new SimplePhone("06", "111"),
					new SimplePhone("02", "222"));
			person1.setBasedn("ou=test," + BASE_DN);

			OdmFriend friend = new OdmFriend();
			friend.setCommonName("imAFriend");
			friend.setSurname("hei");
			friend.setTelephoneNumber(new SimplePhone("06", "333"),
					new SimplePhone("02", "444"));
			friend.setBasedn("cn=imAPersonOne,ou=test," + BASE_DN);

			person1.setFriends(friend);

			OdmPerson person2 = new OdmPerson();
			person2.setCommonName("imAPersonTwo");
			person2.setSurname("peace");
			person2.setTelephoneNumber(new SimplePhone("06", "555"),
					new SimplePhone("02", "666"));
			person2.setBasedn("ou=test," + BASE_DN);

			organizationalUnit.setPersons(person1, person2);

			odmConnection.add(organizationalUnit);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void test2_Update() {
		try {
			OdmPerson person = new OdmPerson();
			person.setCommonName("imAPersonOne");
			person.setSurname("love");
			person.setBasedn("ou=test," + BASE_DN);

			odmConnection.update(person, false);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void test3_Fetch() {
		try {
			OdmOrganizationalUnit organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn(BASE_DN);

			odmConnection.fetch(organizationalUnit, OdmChildScope.SUB);

			if (!organizationalUnit.getPersons()[0].getSurname().equals("love")) {
				fail("Expected 'love' but got: " + organizationalUnit.getPersons()[0].getSurname());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void test4_Search() {
		try {
			OdmSearchResult result = odmConnection.search("ou=test," + BASE_DN,
					OdmAbstractPerson.class, OdmSearchScope.SUB, "cn=*", null);

			assertTrue("Expected 3 entries, got: " + result.getEntryCount(), 
					result.getEntryCount() == 3);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void test5_Remove() {
		try {
			// Remove children first (friend)
			OdmFriend friend = new OdmFriend();
			friend.setCommonName("imAFriend");
			friend.setBasedn("cn=imAPersonOne,ou=test," + BASE_DN);
			odmConnection.remove(friend);

			// Remove persons
			OdmPerson person1 = new OdmPerson();
			person1.setCommonName("imAPersonOne");
			person1.setBasedn("ou=test," + BASE_DN);
			odmConnection.remove(person1);

			OdmPerson person2 = new OdmPerson();
			person2.setCommonName("imAPersonTwo");
			person2.setBasedn("ou=test," + BASE_DN);
			odmConnection.remove(person2);

			// Remove OU
			OdmOrganizationalUnit organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn(BASE_DN);
			odmConnection.remove(organizationalUnit);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	// Listener methods (original)

	@Override
	public void onConnectionClose(String ldapUrl, String username, String cause) {
		System.out.println("onConnectionClose(" + ldapUrl + ", " + username + ", " + cause + ")");
	}

	@Override
	public void onConnectionOpen(String ldapUrl, String username) {
		System.out.println("onConnectionOpen(" + ldapUrl + ", " + username + ")");
	}

	@Override
	public void onAdd(String dn, Object pojo, String username) {
		System.out.println(username + ": onAdd(" + dn + "; " + pojo.getClass() + ")");
	}

	@Override
	public void onRemove(String dn, Class<?> pojoClass, String username) {
		System.out.println(username + ": onRemove(" + dn + "; " + pojoClass + ")");
	}

	@Override
	public void onUpdate(String dn, Object pojo, String username) {
		System.out.println(username + ": onUpdate(" + dn + "; " + pojo.getClass() + ")");
	}
}
