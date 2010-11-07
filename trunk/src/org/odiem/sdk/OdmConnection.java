package org.odiem.sdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.odiem.api.OdmStack;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmChildScope;
import org.odiem.sdk.beans.OdmResult;
import org.odiem.sdk.beans.OdmSearchResult;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;
import org.odiem.sdk.mappers.ChildMapper;

public final class OdmConnection {

	private OdmStack odmStack;
	private boolean statisticsEnabled;
	private OdmConnectionListener connectionListener;

	public OdmConnection(OdmStack odmStack, boolean statisticsEnabled) {
		this.odmStack = odmStack;
		this.statisticsEnabled = statisticsEnabled;
	}

	public void setConnectionListener(OdmConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
		odmStack.setStackListener(connectionListener);
	}

	public boolean isStatisticsEnabled() {
		return statisticsEnabled;
	}

	public void setStatisticsEnabled(boolean statisticsEnabled) {
		this.statisticsEnabled = statisticsEnabled;
	}

	public OdmConnection createProxiedConnection(String proxiedUsername)
			throws OdmException {
		OdmConnection odmConnection;
		try {
			odmConnection = new OdmConnection(
					odmStack.createProxy(proxiedUsername), statisticsEnabled);
			odmConnection.setConnectionListener(connectionListener);
			return odmConnection;
		} catch (Exception e) {
			throw new OdmException(e);
		}

	}

	public <T> OdmResult add(T pojo) throws OdmException {
		return add(pojo, true);
	}

	public <T> OdmResult add(T pojo, boolean addChilds) throws OdmException {
		try {
			long t = 0, s = 0;
			if (statisticsEnabled) {
				t = System.currentTimeMillis();
			}
			@SuppressWarnings("unchecked")
			OdmPojo<T> odmpojo = (OdmPojo<T>) OdmPojo.getInstance(pojo
					.getClass());

			String dn = odmpojo.dn(pojo);
			List<OdmAttribute> attributes = odmpojo.getAttributes(pojo, true);

			if (statisticsEnabled) {
				s = System.currentTimeMillis();
			}
			odmStack.add(dn, attributes);
			if (statisticsEnabled) {
				s = System.currentTimeMillis() - s;
			}

			// notify
			if (connectionListener != null) {
				connectionListener.onAdd(dn, pojo,
						odmStack.getCurrentUsername());
			}

			// adding childs
			if (addChilds) {
				Object[] pojoChilds = odmpojo.getChilds(pojo);
				for (Object child : pojoChilds) {
					OdmResult odmResult = add(child, true);

					if (statisticsEnabled) {
						s += odmResult.getStackExecutionTime();
					}
				}
			}

			if (statisticsEnabled) {
				t = System.currentTimeMillis() - t;
			}

			return new OdmResult(dn, t, s);
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

	public <T> OdmResult update(T pojo) throws OdmException {
		return update(pojo, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> OdmResult update(T pojo, boolean updateChilds)
			throws OdmException {
		try {
			long t = 0, s = 0;
			if (statisticsEnabled) {
				t = System.currentTimeMillis();
			}
			OdmPojo<T> odmpojo = (OdmPojo<T>) OdmPojo.getInstance(pojo
					.getClass());

			String dn = odmpojo.dn(pojo);
			List<OdmAttribute> attributes = odmpojo.getAttributes(pojo, false);
			boolean added = false;
			if (statisticsEnabled) {
				s = System.currentTimeMillis();
			}
			try {
				odmStack.modify(dn, attributes);
			} catch (Exception e) {
				// maybe not exists... trying to insert
				odmStack.add(dn, attributes);
				added = true;
			}
			if (statisticsEnabled) {
				s = System.currentTimeMillis() - s;
			}

			// notify
			if (connectionListener != null) {
				if (added) {
					connectionListener.onAdd(dn, pojo,
							odmStack.getCurrentUsername());
				} else {
					connectionListener.onUpdate(dn, pojo,
							odmStack.getCurrentUsername());
				}
			}

			// updating childs
			if (updateChilds) {

				Object[] pojoChilds = odmpojo.getChilds(pojo);
				HashSet<String> dnToCheck = new HashSet<String>();
				for (Object child : pojoChilds) {
					OdmResult tmpResult = update(child, true);
					dnToCheck.add(tmpResult.getDn());
				}

				// removing unmodified childs
				ArrayList<ChildMapper> childMappers = odmpojo.getChilds();
				for (ChildMapper childMapper : childMappers) {

					OdmPojo childOdmPojo = childMapper.getChildOdmPojo();

					// searching all subnode
					OdmSearchResult childsToCheck = search(dn,
							childOdmPojo.getPojoClass(), OdmSearchScope.ONE,
							null,
							new String[] { childOdmPojo.getDnAttribute() });

					if (statisticsEnabled) {
						s += childsToCheck.getStackExecutionTime();
					}

					// check and remove
					for (Object childToCheck : childsToCheck.getSearchEntries()) {
						if (!dnToCheck.remove(childOdmPojo.dn(childToCheck))) {
							OdmResult odmResult = remove(childToCheck);
							if (statisticsEnabled) {
								s += odmResult.getStackExecutionTime();
							}
						}
					}
				}
			}

			return new OdmResult(dn, t, s);
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

	public <T> OdmResult remove(T pojo) throws OdmException {
		try {
			long t = 0, s = 0;
			if (statisticsEnabled) {
				t = System.currentTimeMillis();
			}
			@SuppressWarnings("unchecked")
			OdmPojo<T> odmpojo = (OdmPojo<T>) OdmPojo.getInstance(pojo
					.getClass());

			String dn = odmpojo.dn(pojo);

			if (statisticsEnabled) {
				s = System.currentTimeMillis();
			}
			odmStack.delete(dn);

			if (statisticsEnabled) {
				t = System.currentTimeMillis() - t;
				s = System.currentTimeMillis() - s;
			}

			// notify
			if (connectionListener != null) {
				// FIXME childs remove notification
				connectionListener.onRemove(dn, pojo.getClass(),
						odmStack.getCurrentUsername());
			}

			return new OdmResult(dn, t, s);
		} catch (Exception e) {
			throw new OdmException(e);
		}

	}

	public <T> OdmResult fetch(T pojo) throws OdmException {
		return fetch(pojo, OdmChildScope.SUB);
	}

	public <T> OdmResult fetch(T pojo, OdmChildScope odmChildScope)
			throws OdmException {
		try {
			long t = 0, s = 0;
			if (statisticsEnabled) {
				t = System.currentTimeMillis();
			}
			@SuppressWarnings("unchecked")
			OdmPojo<T> odmpojo = (OdmPojo<T>) OdmPojo.getInstance(pojo
					.getClass());

			String dn = odmpojo.dn(pojo);
			String[] attributesToRetrieve = odmpojo.getAttributes();
			if (statisticsEnabled) {
				s = System.currentTimeMillis();
			}
			OdmSearchResultEntry result = odmStack.getEntry(dn,
					attributesToRetrieve);
			if (statisticsEnabled) {
				s = System.currentTimeMillis() - s;
			}

			odmpojo.fillPojo(pojo, null, result.getAttributes());

			// fetching childs
			OdmResult odmResult = fetchChilds(pojo, odmChildScope);

			if (statisticsEnabled) {
				s += odmResult.getStackExecutionTime();
			}

			if (statisticsEnabled) {
				t = System.currentTimeMillis() - t;
			}
			return new OdmResult(dn, t, s);

		} catch (Exception e) {
			throw new OdmException(e);
		}

	}

	public <T> OdmResult fetchChilds(T pojo, OdmChildScope childScope)
			throws OdmException {
		try {
			long t = 0, s = 0;
			if (statisticsEnabled) {
				t = System.currentTimeMillis();
			}
			@SuppressWarnings("unchecked")
			OdmPojo<T> odmpojo = (OdmPojo<T>) OdmPojo.getInstance(pojo
					.getClass());

			ArrayList<ChildMapper> childs = odmpojo.getChilds();

			String dn = odmpojo.dn(pojo);

			if (childScope != OdmChildScope.NO_CHILDS) {

				for (ChildMapper childMapper : childs) {

					OdmSearchResult searchResult = search(dn,
							childMapper.getCoreClass(), OdmSearchScope.ONE,
							null, null);

					if (statisticsEnabled) {
						s += searchResult.getStackExecutionTime();
					}

					// fetching childs
					switch (childScope) {
					case ONE:
						for (Object childPojo : searchResult.getSearchEntries()) {

							OdmResult odmResult = fetchChilds(childPojo,
									OdmChildScope.NO_CHILDS);

							if (statisticsEnabled) {
								s += odmResult.getStackExecutionTime();
							}
						}
						break;
					case SUB:
						for (Object childPojo : searchResult.getSearchEntries()) {

							OdmResult odmResult = fetchChilds(childPojo,
									OdmChildScope.SUB);

							if (statisticsEnabled) {
								s += odmResult.getStackExecutionTime();
							}
						}
						break;
					case NO_CHILDS:
					default:
						break;
					}

					odmpojo.fillChild(searchResult.getSearchEntries(),
							childMapper, pojo);
				}
			}
			return new OdmResult(dn, t, s);
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

	public <T> OdmSearchResult search(String baseDn, Class<T> pojoClass,
			OdmSearchScope searchscope, String filter, String[] atrributes)
			throws OdmException {

		try {
			long t = 0, s = 0;
			if (statisticsEnabled) {
				t = System.currentTimeMillis();
				s = System.currentTimeMillis();
			}

			OdmPojo<T> odmpojo = OdmPojo.getInstance(pojoClass);

			// objectClass in filter to optimize search
			if (odmpojo.getLdapClass() != null) {
				StringBuffer buffer = new StringBuffer("(objectClass=").append(
						odmpojo.getLdapClass()).append(")");

				if (filter != null && filter.length() > 0) {
					buffer.insert(0, "(&", 0, 2).append("(").append(filter)
							.append("))");
				}

				filter = buffer.toString();
			}

			if (atrributes == null) {
				atrributes = odmpojo.getAttributes();
			} else if (atrributes.length == 0) {
				atrributes = new String[] { odmpojo.getDnAttribute() };
			}

			List<OdmSearchResultEntry> searchResults = odmStack.search(baseDn,
					searchscope, filter, atrributes);

			if (statisticsEnabled) {
				s = System.currentTimeMillis() - s;
			}

			ArrayList<Object> list = new ArrayList<Object>();

			for (OdmSearchResultEntry searchResultEntry : searchResults) {

				T pojo = odmpojo.createAndFillPojo(searchResultEntry.getDn(),
						searchResultEntry.getAttributes());

				list.add(pojo);
			}
			OdmSearchResult result = new OdmSearchResult(baseDn, list);

			if (statisticsEnabled) {
				t = System.currentTimeMillis() - t;
				result.setExecutionTime(t);
				result.setStackExecutionTime(s);
			}

			return result;
		} catch (Exception e) {
			throw new OdmException(e);
		}

	}

	public void close() throws Exception {
		odmStack.close();
	}

}
