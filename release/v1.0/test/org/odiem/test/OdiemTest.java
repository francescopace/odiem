package org.odiem.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyEditorManager;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odiem.OdmDriverManager;
import org.odiem.sdk.OdmConnection;
import org.odiem.sdk.OdmConnectionFactory;
import org.odiem.sdk.OdmConnectionListener;
import org.odiem.sdk.beans.OdmChildScope;
import org.odiem.sdk.beans.OdmSearchResult;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.stacks.unbounid.DriverImpl;
import org.odiem.test.editors.SimplePhoneEditor;
import org.odiem.test.pojo.OdmAbstractPerson;
import org.odiem.test.pojo.OdmFriend;
import org.odiem.test.pojo.OdmOrganizationalUnit;
import org.odiem.test.pojo.OdmPerson;
import org.odiem.test.pojo.SimplePhone;

public class OdiemTest implements OdmConnectionListener {

	private static final String HOST = "localhost";
	private static final int PORT = 1389;
	private static final String USERNAME = "cn=Manager";
	private static final String PASSWORD = "secret";

	private static OdmConnection odmConnection;

	@BeforeClass
	public static void setUp() {
		try {

			PropertyEditorManager.registerEditor(SimplePhone.class,
					SimplePhoneEditor.class);
			// or you can set EditorSearchPath
			// PropertyEditorManager.setEditorSearchPath(new
			// String[]{"sun.beans.editors","org.odiem.test.editors"});

			System.out.println(OdmDriverManager.getDrivers());

			Properties properties = new Properties();
			properties.put(DriverImpl.PROPS.AUTO_RECONNECT, "false");
			properties.put(DriverImpl.PROPS.POOL_SIZE, "10");

			OdmConnectionFactory odmFactory = OdmDriverManager
					.getConnectionFactory("unboundid.odm.driver", properties);

			odmConnection = odmFactory.createConnection(HOST, PORT, USERNAME,
					PASSWORD);

			odmConnection.setConnectionListener(new OdiemTest());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void cleanUp() {
		try {
			if (odmConnection != null) {
				odmConnection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAdd() {
		try {
			OdmOrganizationalUnit organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn("dc=cab");

			OdmPerson person1 = new OdmPerson();
			person1.setCommonName("imAPersonOne");
			person1.setSurname("peace");
			person1.setTelephoneNumber(new SimplePhone("06", "111"),
					new SimplePhone("02", "222"));
			person1.setBasedn("ou=test,dc=cab");

			OdmFriend friend = new OdmFriend();
			friend.setCommonName("imAFriend");
			friend.setSurname("hei");
			friend.setTelephoneNumber(new SimplePhone("06", "333"),
					new SimplePhone("02", "444"));
			friend.setBasedn("cn=imAPersonOne,ou=test,dc=cab");

			person1.setFriends(friend);

			OdmPerson person2 = new OdmPerson();
			person2.setCommonName("imAPersonTwo");
			person2.setSurname("peace");
			person2.setTelephoneNumber(new SimplePhone("06", "555"),
					new SimplePhone("02", "666"));
			person2.setBasedn("ou=test,dc=cab");

			organizationalUnit.setPersons(person1, person2);

			odmConnection.add(organizationalUnit);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

//	@Test
	public void testProxy() {
		try {
			odmConnection = odmConnection
					.createProxiedConnection("ou=test,dc=cab");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testUpdate() {
		try {
			OdmPerson person = new OdmPerson();
			person.setCommonName("imAPersonOne");
			person.setSurname("love");
			person.setBasedn("ou=test,dc=cab");

			odmConnection.update(person, false);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testFetch() {
		try {

			OdmOrganizationalUnit organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn("dc=cab");

			odmConnection.fetch(organizationalUnit, OdmChildScope.SUB);

			// JAXB.marshal(organizationalUnit, System.out);

			if (!organizationalUnit.getPersons()[0].getSurname().equals("love")) {
				fail(organizationalUnit.getPersons()[0].getSurname());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testSearch() {
		try {

			OdmSearchResult result = odmConnection.search("ou=test,dc=cab",
					OdmAbstractPerson.class, OdmSearchScope.SUB, "cn=*", null);

			// for (Object object : result.getSearchEntries()) {
			// JAXB.marshal(object, System.out);
			// }

			assertTrue(result.getEntryCount() == 3);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRemove() {
		try {
			OdmOrganizationalUnit organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn("dc=cab");

			odmConnection.remove(organizationalUnit);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	// listener methods

	@Override
	public void onConnectionClose(String ldapUrl, String username, String cause) {
		System.out.println("onConnectionClose(" + ldapUrl + ", " + username
				+ ", " + cause + ")");
	}

	@Override
	public void onConnectionOpen(String ldapUrl, String username) {
		System.out.println("onConnectionOpen(" + ldapUrl + ", " + username
				+ ")");
	}

	@Override
	public void onAdd(String dn, Object pojo, String username) {
		System.out.println(username + ": onAdd(" + dn + "; " + pojo.getClass()
				+ ")");
	}

	@Override
	public void onRemove(String dn, Class<?> pojoClass, String username) {
		System.out.println(username + ": onRemove(" + dn + "; " + pojoClass
				+ ")");
	}

	@Override
	public void onUpdate(String dn, Object pojo, String username) {
		System.out.println(username + ": onUpdate(" + dn + "; "
				+ pojo.getClass() + ")");
	}
}
