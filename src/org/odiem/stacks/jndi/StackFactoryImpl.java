package org.odiem.stacks.jndi;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackFactory;
import org.odiem.sdk.exceptions.OdmException;

public class StackFactoryImpl implements OdmStackFactory {

	private Properties properties;

	public StackFactoryImpl(Properties properties) {
		this.properties = properties;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public OdmStack createStack(String host, int port) throws OdmException {
		return createStack(host, port, null, null);
	}

	@Override
	public OdmStack createStack(String host, int port, String username,
			String password) throws OdmException {
		try {

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port);

			if (username != null && password != null) {
				env.put(Context.SECURITY_AUTHENTICATION, "simple");
				env.put(Context.SECURITY_PRINCIPAL, username);
				env.put(Context.SECURITY_CREDENTIALS, password);
			}

			InitialLdapContext ldapContext = new InitialLdapContext(env, null);
			
			return new StackImpl(ldapContext,username);

		} catch (Exception e) {
			throw new OdmException(e);
		}
	}
}
