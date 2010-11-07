package org.odiem.test;
import java.beans.PropertyEditorManager;
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
import org.odiem.test.editors.SimplePhoneEditor;
import org.odiem.test.pojo.OdmFriend;
import org.odiem.test.pojo.OdmOrganizationalUnit;
import org.odiem.test.pojo.OdmPerson;
import org.odiem.test.pojo.SimplePhone;

public class OverheadTest {

	private static final long TIME = 10000;

	private static final String HOST = "localhost";
	private static final int PORT = 1389;
	private static final String USERNAME = "cn=Manager";
	private static final String PASSWORD = "secret";

	private static InitialLdapContext ldapContext; // sun
	private static OdmConnection odmJndiConnection;

	private static OdmOrganizationalUnit organizationalUnit;

	private static boolean stop = false;

	@BeforeClass
	public static void setUp() throws Exception {
		try {
			
			PropertyEditorManager.registerEditor(SimplePhone.class, SimplePhoneEditor.class);
			
			// sun
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, "ldap://" + HOST + ":" + PORT);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, USERNAME);
			env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
			ldapContext = new InitialLdapContext(env, null);

			// odiem
			OdmConnectionFactory odmFactory = OdmDriverManager
					.getConnectionFactory("jndi.odm.driver", null);
			odmJndiConnection = odmFactory.createConnection(HOST, PORT,
					USERNAME, PASSWORD, false);
			
			//prepare a pojo

			organizationalUnit = new OdmOrganizationalUnit();
			organizationalUnit.setName("test");
			organizationalUnit.setBasedn("dc=cab");

			OdmPerson person = new OdmPerson();
			person.setCommonName("imAPersonOne");
			person.setSurname("peace");
			person.setTelephoneNumber(new SimplePhone("06","111"), new SimplePhone("02","222"));
			person.setBasedn("ou=test,dc=cab");

			organizationalUnit.setPersons(person);

			odmJndiConnection.add(organizationalUnit);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		try {
			ldapContext.close();
			odmJndiConnection.remove(organizationalUnit);
			odmJndiConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void jndi() throws Exception {
		try {

			int n = 0;
			Timer.go();
			while (!stop) {
				ldapContext.getAttributes("cn=imAPersonOne,ou=test,dc=Cab");
				n++;
			}
			System.out.println("jndi: " + n);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void odiem() throws Exception {
		try {
			OdmFriend pojo = new OdmFriend();
			pojo.setCommonName("imAPersonOne");
			pojo.setBasedn("ou=test,dc=Cab");
			
			int n = 0;
			Timer.go();
			while (!stop) {
				odmJndiConnection.fetch(pojo, OdmChildScope.NO_CHILDS);
				n++;
			}
			System.out.println("odiem(jndi): " + n);
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
