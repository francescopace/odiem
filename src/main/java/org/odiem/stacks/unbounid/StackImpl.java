package org.odiem.stacks.unbounid;

import java.util.List;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackListener;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.controls.ProxiedAuthorizationV1RequestControl;
import com.unboundid.ldap.sdk.controls.SubtreeDeleteRequestControl;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPException;

public class StackImpl implements OdmStack {

	private LDAPInterface ldapInterface;
	private String username;
	private Control[] controls;
	private OdmStackListener stackListener;

	public StackImpl(LDAPInterface ldapInterface, String username,
			Control... controls) {
		this.ldapInterface = ldapInterface;
		this.username = username;
		this.controls = controls;
	}

	@Override
	public void add(String dn, List<OdmAttribute> attributes) throws Exception {
		AddRequest addRequest = new AddRequest(dn,
				Converter.odmAttributeToAttributes(attributes), controls);
		ldapInterface.add(addRequest);
	}

	@Override
	public void modify(String dn, List<OdmAttribute> attributes)
			throws Exception {
		ModifyRequest modifyrequest = new ModifyRequest(dn,
				Converter.odmAttributesToModifications(attributes), controls);
		ldapInterface.modify(modifyrequest);
	}

	@Override
	public void delete(String dn) throws Exception {
		DeleteRequest deleteRequest = new DeleteRequest(dn, controls);
		deleteRequest.addControl(new SubtreeDeleteRequestControl());
		ldapInterface.delete(deleteRequest);
	}

	@Override
	public OdmSearchResultEntry getEntry(String dn,
			String[] attributesToRetrieve) throws Exception {

		List<OdmSearchResultEntry> results = search(dn, OdmSearchScope.BASE,
				"objectClass=*", attributesToRetrieve);

		if (!results.isEmpty()) {
			return results.get(0);
		} else {
			throw new LDAPException("not found");
		}
	}

	@Override
	public List<OdmSearchResultEntry> search(String baseDn,
			OdmSearchScope searchscope, String filter, String[] atrributes)
			throws Exception {
		SearchRequest searchRequest = Converter.prepareSearchRequest(baseDn,
				searchscope, filter, atrributes);

		searchRequest.addControls(controls);

		return Converter.searchResultToOdmSearchResultEntry(ldapInterface
				.search(searchRequest));
	}

	@Override
	public void close() {
		if (ldapInterface instanceof LDAPConnection) {
			((LDAPConnection) ldapInterface).close();
		}
	}

	@Override
	public String getCurrentUsername() {
		return username;
	}

	@Override
	public OdmStack createProxy(String proxiedUsername) {
		return new StackImpl(ldapInterface, proxiedUsername,
				new ProxiedAuthorizationV1RequestControl(proxiedUsername));
	}

	@Override
	public void setStackListener(OdmStackListener stackListener) {
		this.stackListener = stackListener;
		// TODO take events
	}

}
