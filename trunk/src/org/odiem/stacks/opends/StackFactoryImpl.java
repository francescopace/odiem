package org.odiem.stacks.opends;

import java.util.Properties;

import org.glassfish.grizzly.TransportFactory;
import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackFactory;
import org.odiem.sdk.exceptions.OdmException;
import org.opends.sdk.Connection;
import org.opends.sdk.LDAPConnectionFactory;

import com.sun.opends.sdk.tools.PerfToolTCPNIOTransportFactory;

public class StackFactoryImpl implements OdmStackFactory {

	private Properties properties;

	public StackFactoryImpl(Properties properties) {
		this.properties = properties;
		TransportFactory.setInstance(new PerfToolTCPNIOTransportFactory());
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
			
			//opends
			LDAPConnectionFactory factory = new LDAPConnectionFactory(
					host, port);
			Connection connection = factory.getConnection();
			if (username != null) {
				connection.bind(username, password.toCharArray());
			}
			return new StackImpl(connection, username);

		} catch (Exception e) {
			throw new OdmException(e);
		}
	}
}
