package org.odiem.stacks.novell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;

public class Converter {

	public static OdmAttribute[] ldapAttributeSetToOdmAttributes(
			LDAPAttributeSet ldapAttributeSet) {
		OdmAttribute[] tmp = null;
		if (ldapAttributeSet != null) {
			tmp = new OdmAttribute[ldapAttributeSet.size()];
			Iterator<?> it = ldapAttributeSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				LDAPAttribute ldapAttribute = (LDAPAttribute) it.next();

				tmp[i] = new OdmAttribute(ldapAttribute.getName(),
						ldapAttribute.getStringValueArray());
				i++;
			}
		}
		return tmp;
	}

	public static LDAPAttributeSet odmAttributeToLDAPAttributeSet(
			List<OdmAttribute> attributes) {

		LDAPAttributeSet tmp = new LDAPAttributeSet();
		if (attributes != null) {

			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					tmp.add(new LDAPAttribute(odmAttribute.getName(),
							odmAttribute.getValues()));
				}
			}
		}
		return tmp;
	}

	public static LDAPModification[] odmAttributesToLDAPModifications(
			List<OdmAttribute> attributes) {
		LDAPModification[] tmp = null;
		if (attributes != null) {
			List<LDAPModification> list = new ArrayList<LDAPModification>();

			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					list.add(new LDAPModification(LDAPModification.REPLACE,
							new LDAPAttribute(odmAttribute.getName(),
									odmAttribute.getValues())));
				} else {
					list.add(new LDAPModification(LDAPModification.DELETE,
							new LDAPAttribute(odmAttribute.getName())));
				}
			}
			tmp = list.toArray(new LDAPModification[list.size()]);
		}
		return tmp;
	}

	public static List<OdmSearchResultEntry> searchLDAPSearchResultsToOdmSearchResultEntry(
			LDAPSearchResults searchResults) throws Exception {

		ArrayList<OdmSearchResultEntry> list = new ArrayList<OdmSearchResultEntry>();

		while (searchResults.hasMore()) {

			LDAPEntry nextEntry = null;
			try {
				nextEntry = searchResults.next();
			} catch (LDAPException e) {
				if (e.getResultCode() == LDAPException.LDAP_TIMEOUT
						|| e.getResultCode() == LDAPException.CONNECT_ERROR)
					break;
				else
					continue;
			}

			list.add(new OdmSearchResultEntry(
					nextEntry.getDN(),
					ldapAttributeSetToOdmAttributes(nextEntry.getAttributeSet())));
		}
		return list;
	}

	public static int convertOdmSearchScope(OdmSearchScope searchscope)
			throws OdmException {
		int scope = LDAPConnection.SCOPE_ONE;
		switch (searchscope) {
		case BASE:
			scope = LDAPConnection.SCOPE_BASE;
			break;
		case ONE:
			scope = LDAPConnection.SCOPE_ONE;
			break;
		case SUB:
			scope = LDAPConnection.SCOPE_SUB;
			break;
		}

		return scope;
	}
}
