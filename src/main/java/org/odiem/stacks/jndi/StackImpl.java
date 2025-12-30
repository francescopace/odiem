package org.odiem.stacks.jndi;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.odiem.api.OdmStack;
import org.odiem.api.OdmStackListener;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.stacks.jndi.controls.ProxyAuthorizationControl;
import org.odiem.stacks.jndi.controls.TreeDeleteControl;

public class StackImpl implements OdmStack {

	private LdapContext ldapContext;
	private String username;
	private OdmStackListener stackListener;

	public StackImpl(LdapContext ldapContext, String username) throws Exception {
		List<Control> controls = new ArrayList<Control>();

		String principal = (String) ldapContext.getEnvironment().get(
				LdapContext.SECURITY_PRINCIPAL);

		// proxied authentication
		if (principal == null || !principal.equals(username)) {
			controls.add(new ProxyAuthorizationControl(username, ldapContext
					.getNameInNamespace()));
		}

		// treedelete
		controls.add(new TreeDeleteControl());

		ldapContext.setRequestControls(controls.toArray(new Control[controls
				.size()]));

		this.ldapContext = ldapContext;
		this.username = username;

	}

	@Override
	public void add(String dn, List<OdmAttribute> attributes) throws Exception {
		ldapContext.bind(dn, null,
				Converter.odmAttributeToAttributes(attributes));
	}

	@Override
	public void modify(String dn, List<OdmAttribute> attributes)
			throws Exception {
		ldapContext.modifyAttributes(dn,
				Converter.odmAttributesToModifications(attributes));
	}

	@Override
	public void delete(String dn) throws Exception {
		ldapContext.unbind(dn);
	}

	@Override
	public OdmSearchResultEntry getEntry(String dn,
			String[] attributesToRetrieve) throws Exception {
		Attributes attributes = ldapContext.getAttributes(dn,
				attributesToRetrieve);
		return new OdmSearchResultEntry(dn,
				Converter.attributesToOdmAttributes(attributes));
	}

	@Override
	public List<OdmSearchResultEntry> search(String baseDn,
			OdmSearchScope searchscope, String filter, String[] atrributes)
			throws Exception {

		return Converter.searchResultToOdmSearchResultEntry(ldapContext.search(
				baseDn, filter,
				Converter.prepareSearchRequest(searchscope, atrributes)));
	}

	@Override
	public void close() throws Exception {
		ldapContext.close();
	}

	@Override
	public String getCurrentUsername() {
		return username;
	}

	@Override
	public OdmStack createProxy(String proxiedUsername) throws Exception {
		return new StackImpl(ldapContext.newInstance(null), proxiedUsername);
	}

	@Override
	public void setStackListener(OdmStackListener stackListener) {
		this.stackListener = stackListener;
		// TODO take events
	}

}
