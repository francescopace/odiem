package org.odiem.stacks.novell;

import java.util.Properties;

import org.odiem.OdmDriverManager;
import org.odiem.api.OdmDriver;
import org.odiem.api.OdmStackFactory;
import org.odiem.sdk.beans.OdmStackPropertyInfo;
import org.odiem.sdk.exceptions.OdmException;

public class DriverImpl implements OdmDriver {

	public static final String DRIVER_NAME = "novell.odm.driver";

	private OdmStackPropertyInfo[] propertyInfos;

	static {
		OdmDriverManager.registerDriver(new DriverImpl());
	}

	@Override
	public String getName() {
		return DRIVER_NAME;
	}

	@Override
	public OdmStackFactory getStackFactory(Properties properties)
			throws OdmException {

		return new StackFactoryImpl(properties);
	}

	@Override
	public OdmStackPropertyInfo[] getStackFactoryPropertyInfo() {
		return propertyInfos;
	}
}
