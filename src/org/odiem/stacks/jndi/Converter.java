package org.odiem.stacks.jndi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;

public class Converter {

	public static OdmAttribute[] attributesToOdmAttributes(Attributes attributes)
			throws NamingException {
		OdmAttribute[] tmp = null;
		if (attributes != null) {
			tmp = new OdmAttribute[attributes.size()];

			int i = 0;
			NamingEnumeration<? extends Attribute> en = attributes.getAll();
			while (en.hasMoreElements()) {
				Attribute attribute = en.nextElement();

				tmp[i] = new OdmAttribute(attribute.getID(), Collections.list(
						attribute.getAll()).toArray(
						new String[attribute.size()]));
				i++;
			}
		}
		return tmp;
	}

	public static Attributes odmAttributeToAttributes(
			List<OdmAttribute> attributes) {

		Attributes tmp = new BasicAttributes();
		if (attributes != null) {

			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					Attribute attribute = new BasicAttribute(
							odmAttribute.getName());
					for (String value : odmAttribute.getValues()) {
						attribute.add(value);
					}
					tmp.put(attribute);
				}
			}
		}
		return tmp;
	}

	public static ModificationItem[] odmAttributesToModifications(
			List<OdmAttribute> attributes) {
		ModificationItem[] tmp = null;
		if (attributes != null) {

			List<ModificationItem> list = new ArrayList<ModificationItem>();

			for (OdmAttribute odmAttribute : attributes) {
				Attribute attribute = new BasicAttribute(odmAttribute.getName());

				if (odmAttribute.getValues().length > 0) {
					for (String value : odmAttribute.getValues()) {
						attribute.add(value);
					}
					list.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
							attribute));
				} else {
					list.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							attribute));
				}

			}
			tmp = list.toArray(new ModificationItem[list.size()]);
		}
		return tmp;
	}

	public static List<OdmSearchResultEntry> searchResultToOdmSearchResultEntry(
			NamingEnumeration<SearchResult> searchResults) throws Exception {

		ArrayList<OdmSearchResultEntry> list = new ArrayList<OdmSearchResultEntry>();

		while (searchResults.hasMoreElements()) {
			SearchResult searchResult = searchResults.nextElement();

			list.add(new OdmSearchResultEntry(
					searchResult.getNameInNamespace(),
					attributesToOdmAttributes(searchResult.getAttributes())));
		}
		return list;
	}

	public static SearchControls prepareSearchRequest(
			OdmSearchScope searchscope, String... attributes)
			throws OdmException {
		try {
			int scope = SearchControls.ONELEVEL_SCOPE;
			switch (searchscope) {
			case BASE:
				scope = SearchControls.OBJECT_SCOPE;
				break;
			case ONE:
				scope = SearchControls.ONELEVEL_SCOPE;
				break;
			case SUB:
				scope = SearchControls.SUBTREE_SCOPE;
				break;
			}

			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(scope);
			searchControls.setReturningAttributes(attributes);

			return searchControls;
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}
}
