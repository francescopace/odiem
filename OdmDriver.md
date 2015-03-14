
```
package org.odiem.api;

import java.util.Properties;

import org.odiem.sdk.beans.OdmStackPropertyInfo;
import org.odiem.sdk.exceptions.OdmException;

public interface OdmDriver {

	public OdmStackFactory getStackFactory(Properties properties)
			throws OdmException;

	public String getName();

	public OdmStackPropertyInfo[] getStackFactoryPropertyInfo();

}
```