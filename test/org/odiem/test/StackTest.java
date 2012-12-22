package org.odiem.test;

import java.beans.PropertyEditorManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odiem.OdmDriverManager;
import org.odiem.sdk.OdmConnection;
import org.odiem.sdk.OdmConnectionFactory;
import org.odiem.sdk.beans.OdmChildScope;
import org.odiem.test.editors.SimplePhoneEditor;
import org.odiem.test.pojo.OdmFriend;
import org.odiem.test.pojo.OdmOrganizationalUnit;
import org.odiem.test.pojo.OdmPerson;
import org.odiem.test.pojo.SimplePhone;

public class StackTest {

	private static final long TIME = 10000;

	private static final String HOST = "localhost";
	private static final int PORT = 389;
	private static final String USERNAME = "cn=Directory Manager";
	private static final String PASSWORD = "secret";

	private static OdmConnection odmUnboundidConnection;
	private static OdmConnection odmJndiConnection;
	private static OdmConnection odmApacheConnection;
	private static OdmConnection odmNovellConnection;
	private static OdmConnection odmOpendsConnection;

	private static OdmOrganizationalUnit organizationalUnit;
	private static OdmFriend pojo;

	private static boolean stop = false;

	
	@BeforeClass
	public static void setUp() throws Exception {
		try {
			PropertyEditorManager.registerEditor(SimplePhone.class, SimplePhoneEditor.class);

			// odiem unboundid
			OdmConnectionFactory odmFactoryU = OdmDriverManager
					.getConnectionFactory("unboundid.odm.driver", null);
			odmUnboundidConnection = odmFactoryU.createConnection(HOST, PORT,
					USERNAME, PASSWORD, true);

			// odiem jndi
			OdmConnectionFactory odmFactoryS = OdmDriverManager
					.getConnectionFactory("jndi.odm.driver", null);
			odmJndiConnection = odmFactoryS.createConnection(HOST, PORT,
					USERNAME, PASSWORD, false);

			// odiem apache
			OdmConnectionFactory odmFactoryA = OdmDriverManager
					.getConnectionFactory("apache.odm.driver", null);
			odmApacheConnection = odmFactoryA.createConnection(HOST, PORT,
					USERNAME, PASSWORD, true);

			// odiem novell
			OdmConnectionFactory odmFactoryN = OdmDriverManager
					.getConnectionFactory("novell.odm.driver", null);
			odmNovellConnection = odmFactoryN.createConnection(HOST, PORT,
					USERNAME, PASSWORD, true);
			
			// odiem opends
			OdmConnectionFactory odmFactoryO = OdmDriverManager
					.getConnectionFactory("opends.odm.driver", null);
			odmOpendsConnection = odmFactoryO.createConnection(HOST, PORT,
					USERNAME, PASSWORD, true);

			// prepare a pojo

			organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn("dc=cab");

			OdmPerson person = new OdmPerson();
			person.setCommonName("imAPersonOne");
			person.setSurname("peace");
			person.setTelephoneNumber(new SimplePhone("06", "111"),
					new SimplePhone("02", "222"));
			person.setBasedn("ou=test,dc=cab");

			organizationalUnit.setPersons(person);

			odmJndiConnection.add(organizationalUnit);
			
			pojo = new OdmFriend();
			pojo.setCommonName("imAPersonOne");
			pojo.setBasedn("ou=test,dc=Cab");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		try {
			odmUnboundidConnection.remove(organizationalUnit);

			odmUnboundidConnection.close();
			odmJndiConnection.close();
			odmApacheConnection.close();
			odmNovellConnection.close();
			odmOpendsConnection.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void odiemUnboundid() throws Exception {
		try {
			int n = 0;
			Timer.go();
			while (!stop) {
				odmUnboundidConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
				n++;
			}
			System.out.println("odiemUnboundidThread: " + n);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void odiemApache() throws Exception {
		try {
			int n = 0;
			Timer.go();
			while (!stop) {
				odmUnboundidConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
				n++;
			}
			System.out.println("odiemApacheThread: " + n);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void odiemJndi() throws Exception {
		try {
			int n = 0;
			Timer.go();
			while (!stop) {
				odmJndiConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
				n++;
			}
			System.out.println("odiemJndiThread: " + n);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void odiemNovell() throws Exception {
		try {
			int n = 0;
			Timer.go();
			while (!stop) {
				odmNovellConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
				n++;
			}
			System.out.println("odiemNovellThread: " + n);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void odiemOpends() throws Exception {
		try {
			int n = 0;
			Timer.go();
			while (!stop) {
				odmOpendsConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
				n++;
			}
			System.out.println("odiemOpendsThread: " + n);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
