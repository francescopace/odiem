package org.odiem;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.odiem.api.OdmDriver;
import org.odiem.sdk.OdmConnectionFactory;
import org.odiem.sdk.exceptions.OdmException;

import sun.misc.Service;

public final class OdmDriverManager {
	

	private static final ConcurrentHashMap<String, OdmDriver> drivers = new ConcurrentHashMap<String, OdmDriver>();
	private static boolean initialized = false;

	public static void registerDriver(OdmDriver driver) {
		initialize();
		drivers.put(driver.getName(), driver);
	}

	public static void deregisterDriver(OdmDriver driver) {
		drivers.remove(driver.getName());
	}

	public static Collection<OdmDriver> getDrivers() {
		initialize();
		return Collections.unmodifiableCollection(drivers.values());
	}

	public static OdmDriver getDriver(String driverName) {
		initialize();
		return drivers.get(driverName);
	}
	
	public static OdmConnectionFactory getConnectionFactory(String driverName) throws OdmException {
		return getConnectionFactory(driverName,null);
	}

	public static OdmConnectionFactory getConnectionFactory(String driverName,
			Properties properties) throws OdmException {

		OdmDriver driver = getDriver(driverName);
		if (driver != null) {
			return new OdmConnectionFactory(driver.getStackFactory(properties));
		} else {
			throw new OdmException("Driver " + driverName + " not registered");
		}
	}

	private static void initialize() {
		if (!initialized) {
			initialized = true;

			Iterator<?> iter = Service.providers(OdmDriver.class);
			while (iter.hasNext()) {
				OdmDriver driver = (OdmDriver) iter.next();
				drivers.put(driver.getName(), driver);
			}
		}
	}

}
