package org.odiem.stacks.opends;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackListener;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.opends.sdk.Connection;
import org.opends.sdk.controls.Control;
import org.opends.sdk.controls.SubtreeDeleteRequestControl;
import org.opends.sdk.requests.DeleteRequest;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.requests.SearchRequest;
import org.opends.sdk.responses.Response;

public class StackImpl implements OdmStack {

	private Connection ldapconnection;
	private String username;
	private Control[] controls;
	private OdmStackListener stackListener;
	
	public StackImpl(Connection connection, String username,
			Control... controls) {
		this.ldapconnection = connection;
		this.username = username;
		this.controls = controls;
	}

	@Override
	public void add(String dn, List<OdmAttribute> attributes) throws Exception {
		ldapconnection
				.add(Converter.createAddRequest(dn, attributes, controls));
	}

	@Override
	public void modify(String dn, List<OdmAttribute> attributes)
			throws Exception {
		ldapconnection.modify(Converter.createModifyRequest(dn, attributes,
				controls));
	}

	@Override
	public void delete(String dn) throws Exception {
		DeleteRequest deleteRequest = Requests.newDeleteRequest(dn);
		for (Control control : controls) {
			deleteRequest.addControl(control);
		}
		deleteRequest.addControl(SubtreeDeleteRequestControl.newControl(false));
		ldapconnection.delete(deleteRequest);
	}

	@Override
	public OdmSearchResultEntry getEntry(String dn,
			String[] attributesToRetrieve) throws Exception {
		List<OdmSearchResultEntry> results = search(dn, OdmSearchScope.BASE,
				"(objectClass=*)", attributesToRetrieve);
		if (!results.isEmpty()) {
			return results.get(0);
		} else {
			throw new Exception("not found");
		}
	}

	@Override
	public List<OdmSearchResultEntry> search(String baseDn,
			OdmSearchScope searchscope, String filter, String[] atrributes)
			throws Exception {
		SearchRequest searchRequest = Converter.prepareSearchRequest(baseDn,
				searchscope, filter, atrributes);

		for (Control control : controls) {
			searchRequest.addControl(control);
		}

		return Converter.searchResponseToOdmSearchResultEntry(ldapconnection
				.search(searchRequest,new LinkedBlockingQueue<Response>()));
	}

	@Override
	public void close() {
		try {
			ldapconnection.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getCurrentUsername() {
		return username;
	}

	@Override
	public OdmStack createProxy(String proxiedUsername) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setStackListener(OdmStackListener stackListener) {
		this.stackListener = stackListener;
		// TODO take events
	}

}
