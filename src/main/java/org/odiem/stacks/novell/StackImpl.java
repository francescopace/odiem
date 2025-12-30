package org.odiem.stacks.novell;

import java.util.ArrayList;
import java.util.List;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackListener;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchConstraints;

public class StackImpl implements OdmStack {

	private LDAPConnection ldapConnection;
	private String username;
	private LDAPConstraints ldapConstraints;
	private OdmStackListener stackListener;

	public StackImpl(LDAPConnection ldapConnection, String username,
			LDAPControl... controls) {
		this.ldapConnection = ldapConnection;
		this.username = username;

		List<LDAPControl> ldapControls = new ArrayList<LDAPControl>();
		ldapControls.add(new LDAPControl("1.2.840.113556.1.4.805", true, null));
		if (controls != null) {
			for (LDAPControl ldapControl : controls) {
				ldapControls.add(ldapControl);
			}
		}
		ldapConstraints = new LDAPConstraints();
		ldapConstraints.setControls(ldapControls
				.toArray(new LDAPControl[ldapControls.size()]));
	}

	@Override
	public void add(String dn, List<OdmAttribute> attributes) throws Exception {
		LDAPEntry entry = new LDAPEntry(dn,
				Converter.odmAttributeToLDAPAttributeSet(attributes));
		ldapConnection.add(entry, ldapConstraints);
	}

	@Override
	public void modify(String dn, List<OdmAttribute> attributes)
			throws Exception {
		LDAPModification[] modifications = Converter
				.odmAttributesToLDAPModifications(attributes);
		ldapConnection.modify(dn, modifications, ldapConstraints);
	}

	@Override
	public void delete(String dn) throws Exception {
		ldapConnection.delete(dn, ldapConstraints);
	}

	@Override
	public OdmSearchResultEntry getEntry(String dn,
			String[] attributesToRetrieve) throws Exception {

		List<OdmSearchResultEntry> results = search(dn, OdmSearchScope.BASE,
				"objectClass=*", attributesToRetrieve);

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

		LDAPSearchConstraints ldapSearchConstraints = new LDAPSearchConstraints(
				ldapConstraints);
		return Converter
				.searchLDAPSearchResultsToOdmSearchResultEntry(ldapConnection
						.search(baseDn,
								Converter.convertOdmSearchScope(searchscope),
								filter, atrributes, false,
								ldapSearchConstraints));
	}

	@Override
	public void close() {
		try {
			ldapConnection.disconnect();
		} catch (LDAPException e) {
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
