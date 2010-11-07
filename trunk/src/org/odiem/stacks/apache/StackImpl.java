package org.odiem.stacks.apache;

import java.io.IOException;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.message.DeleteRequest;
import org.apache.directory.ldap.client.api.message.SearchRequest;
import org.apache.directory.shared.ldap.codec.controls.ControlImpl;
import org.apache.directory.shared.ldap.message.control.Control;
import org.apache.directory.shared.ldap.name.DN;
import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackListener;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;

public class StackImpl implements OdmStack {

	private LdapConnection ldapconnection;
	private String username;
	private Control[] controls;
	private OdmStackListener stackListener;
	
	public StackImpl(LdapConnection connection, String username,
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
		DeleteRequest deleteRequest = new DeleteRequest(new DN(dn));
		deleteRequest.add(controls);
		deleteRequest.add(new ControlImpl("1.2.840.113556.1.4.805"));
//		ldapconnection.delete(deleteRequest);
		ldapconnection.deleteTree(dn);
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

		searchRequest.add(controls);

		return Converter.searchResponseToOdmSearchResultEntry(ldapconnection
				.search(searchRequest));
	}

	@Override
	public void close() {
		try {
			ldapconnection.close();
		} catch (IOException e) {
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
