package org.odiem.stacks.novell;

import java.util.Properties;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackFactory;
import org.odiem.sdk.exceptions.OdmException;

import com.novell.ldap.LDAPConnection;

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

			LDAPConnection lc = new LDAPConnection();
			lc.connect(host, port);

			if (username != null) {
				lc.bind(LDAPConnection.LDAP_V3, username,
						password.getBytes("UTF8"));
			}

			return new StackImpl(lc, username);

		} catch (Exception e) {
			throw new OdmException(e);
		}
	}
}
