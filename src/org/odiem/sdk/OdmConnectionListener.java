package org.odiem.sdk;

import org.odiem.api.OdmStackListener;

public interface OdmConnectionListener extends OdmStackListener {
	public void onAdd(String dn, Object pojo, String owner);

	public void onRemove(String dn, Class<?> pojoclass, String owner);

	public void onUpdate(String dn, Object pojo, String owner);
}
