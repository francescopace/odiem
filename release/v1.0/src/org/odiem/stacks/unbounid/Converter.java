package org.odiem.stacks.unbounid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class Converter {

	public static OdmAttribute[] attributesToOdmAttributes(
			Collection<Attribute> attributes) {
		OdmAttribute[] tmp = null;
		if (attributes != null) {
			tmp = new OdmAttribute[attributes.size()];

			int i = 0;
			for (Attribute attribute : attributes) {
				tmp[i] = new OdmAttribute(attribute.getName(),
						attribute.getValues());
				i++;
			}
		}
		return tmp;
	}

	public static Attribute[] odmAttributeToAttributes(
			List<OdmAttribute> attributes) {

		Attribute[] tmp = null;
		if (attributes != null) {
			List<Attribute> list = new ArrayList<Attribute>();

			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					list.add(new Attribute(odmAttribute.getName(), odmAttribute
							.getValues()));
				}
			}
			tmp = list.toArray(new Attribute[list.size()]);
		}
		return tmp;
	}

	public static Modification[] odmAttributesToModifications(
			List<OdmAttribute> attributes) {
		Modification[] tmp = null;
		if (attributes != null) {
			List<Modification> list = new ArrayList<Modification>();

			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					list.add(new Modification(ModificationType.REPLACE,
							odmAttribute.getName(), odmAttribute.getValues()));
				} else {
					list.add(new Modification(ModificationType.DELETE,
							odmAttribute.getName()));
				}
			}
			tmp = list.toArray(new Modification[list.size()]);
		}
		return tmp;
	}

	public static List<OdmSearchResultEntry> searchResultToOdmSearchResultEntry(
			SearchResult searchResult) throws Exception {

		ArrayList<OdmSearchResultEntry> list = new ArrayList<OdmSearchResultEntry>();

		for (SearchResultEntry searchResultEntry : searchResult
				.getSearchEntries()) {

			list.add(new OdmSearchResultEntry(
					searchResultEntry.getDN(),
					attributesToOdmAttributes(searchResultEntry.getAttributes())));
		}
		return list;
	}

	public static SearchRequest prepareSearchRequest(String baseDn,
			OdmSearchScope searchscope, String filter, String... attributes)
			throws OdmException {
		try {
			SearchScope scope = SearchScope.ONE;
			switch (searchscope) {
			case BASE:
				scope = SearchScope.BASE;
				break;
			case ONE:
				scope = SearchScope.ONE;
				break;
			case SUB:
				scope = SearchScope.SUB;
				break;
			}

			return new SearchRequest(baseDn, scope, filter, attributes);
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}
}