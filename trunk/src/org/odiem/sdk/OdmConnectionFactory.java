package org.odiem.sdk;

import org.odiem.api.OdmStackFactory;
import org.odiem.sdk.exceptions.OdmException;

public final class OdmConnectionFactory {

	private OdmStackFactory stackFactory;

	public OdmConnectionFactory(OdmStackFactory stackFactory) {
		this.stackFactory = stackFactory;
	}

	public OdmStackFactory getStackFactory() {
		return stackFactory;
	}

	public OdmConnection createConnection(String host, int port)
			throws OdmException {
		return createConnection(host, port, null, null, false);
	}

	public OdmConnection createConnection(String host, int port,
			boolean statisticsEnabled) throws OdmException {
		return createConnection(host, port, null, null, statisticsEnabled);
	}

	public OdmConnection createConnection(String host, int port,
			String username, String password) throws OdmException {
		return createConnection(host, port, username, password, false);
	}

	public OdmConnection createConnection(String host, int port,
			String username, String password, boolean statisticsEnabled)
			throws OdmException {
		try {
			return new OdmConnection(stackFactory.createStack(host, port,
					username, password), statisticsEnabled);
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

}
