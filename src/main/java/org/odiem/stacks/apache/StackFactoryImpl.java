package org.odiem.stacks.apache;

import java.util.Properties;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
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

			LdapConnection connection = new LdapNetworkConnection(host, port);
			if (username != null) {
				connection.bind(username, password);
			}
			return new StackImpl(connection, username);

		} catch (Exception e) {
			throw new OdmException(e);
		}
	}
}
