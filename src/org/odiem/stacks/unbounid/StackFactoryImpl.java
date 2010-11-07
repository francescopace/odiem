package org.odiem.stacks.unbounid;

import java.util.Properties;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackFactory;
import org.odiem.sdk.exceptions.OdmException;
import org.odiem.stacks.unbounid.DriverImpl.PROPS;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;

public class StackFactoryImpl implements OdmStackFactory {

	private Properties properties;
	private LDAPConnectionOptions connectionOptions;
	private int connectionPoolSize = 1;

	public StackFactoryImpl(Properties properties) {
		this.properties = properties;

		LDAPConnectionOptions connectionOptions = new LDAPConnectionOptions();
		if (properties != null) {

			for (PROPS prop : PROPS.values()) {

				String value = properties.getProperty(prop.getPropName(),
						prop.getDefaultValue());

				switch (prop) {
				case AUTO_RECONNECT:
					connectionOptions.setAutoReconnect(Boolean
							.getBoolean(value));
					break;
				case POOL_SIZE:
					connectionPoolSize = Integer.parseInt(value);
					break;
				}
			}
		}
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

			LDAPInterface ldapInterface;

			if (username == null && password == null) {
				ldapInterface = new LDAPConnection(connectionOptions, host,
						port);
			} else {
				ldapInterface = new LDAPConnection(connectionOptions, host,
						port, username, password);
			}

			// Load Balancer
			if (connectionPoolSize > 1) {
				ldapInterface = new LDAPConnectionPool(
						(LDAPConnection) ldapInterface, connectionPoolSize);
			}

			return new StackImpl(ldapInterface, username);

		} catch (LDAPException e) {
			throw new OdmException(e);
		}
	}
}