# OdmStack interface. #

```
package org.odiem.api;

import java.util.List;

import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;

public interface OdmStack {

	public void add(String dn, List<OdmAttribute> attributes) throws Exception;

	public void modify(String dn, List<OdmAttribute> attributes)
			throws Exception;

	public void delete(String dn) throws Exception;

	public OdmSearchResultEntry getEntry(String dn,
			String[] attributesToRetrieve) throws Exception;

	public List<OdmSearchResultEntry> search(String baseDn,
			OdmSearchScope searchscope, String filter, String[] atrributes)
			throws Exception;

	public void setStackListener(OdmStackListener stackListener);

	public void close() throws Exception;

	public String getCurrentUsername();

	public OdmStack createProxy(String proxiedUsername) throws Exception;

}
```