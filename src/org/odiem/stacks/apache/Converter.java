package org.odiem.stacks.apache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.DefaultAttribute;
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.entry.ModificationOperation;
import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.SearchRequest;
import org.apache.directory.shared.ldap.model.message.SearchRequestImpl;
import org.apache.directory.shared.ldap.model.message.SearchResultEntry;
import org.apache.directory.shared.ldap.model.message.SearchScope;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;

public class Converter {

	public static OdmAttribute[] entryToOdmAttributes(Entry entry) {
		OdmAttribute[] tmp = null;
		if (entry != null) {
			tmp = new OdmAttribute[entry.size()];
			Iterator<Attribute> it = entry.iterator();
			int i = 0;
			while (it.hasNext()) {
				Attribute entryAttribute = it.next();
				Iterator<Value<?>> eit = entryAttribute.iterator();
				String[] values = new String[entryAttribute.size()];

				int j = 0;
				while (eit.hasNext()) {
					values[j] = eit.next().getString();
					j++;
				}

				tmp[i] = new OdmAttribute(entryAttribute.getId(), values);
				i++;
			}
		}
		return tmp;
	}

	public static AddRequest createAddRequest(String dn,
			List<OdmAttribute> attributes, Control... controls)
			throws Exception {

		Entry tmp = new DefaultEntry(new Dn(dn));
		if (attributes != null) {
			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					tmp.add(odmAttribute.getName(), odmAttribute.getValues());
				}
			}
		}
		AddRequest addRequest = new AddRequestImpl();
		addRequest.setEntry(tmp);
		addRequest.addAllControls(controls);

		return addRequest;
	}

	public static ModifyRequest createModifyRequest(String dn,
			List<OdmAttribute> attributes, Control... controls)
			throws Exception {
		ModifyRequest tmp = new ModifyRequestImpl();
		tmp.setName(new Dn(dn));
		tmp.addAllControls(controls);
		if (attributes != null) {
			for (OdmAttribute odmAttribute : attributes) {
				Attribute attribute = new DefaultAttribute(
						odmAttribute.getName(), odmAttribute.getValues());
				if (odmAttribute.getValues().length > 0) {
					tmp.addModification(attribute,
							ModificationOperation.REPLACE_ATTRIBUTE);
				} else {
					tmp.addModification(attribute,
							ModificationOperation.REMOVE_ATTRIBUTE);
				}
			}
		}
		return tmp;
	}

	public static List<OdmSearchResultEntry> searchResponseToOdmSearchResultEntry(
			SearchCursor searchCursor) throws Exception {

		ArrayList<OdmSearchResultEntry> list = new ArrayList<OdmSearchResultEntry>();

		while (searchCursor.next()) {
			SearchResultEntry searchResultEntry = (SearchResultEntry) searchCursor
					.get();

			list.add(new OdmSearchResultEntry(searchResultEntry.getObjectName()
					.toString(), entryToOdmAttributes(searchResultEntry
					.getEntry())));
		}
		return list;
	}

	public static SearchRequest prepareSearchRequest(String baseDn,
			OdmSearchScope searchscope, String filter, String... attributes)
			throws OdmException {
		try {
			SearchScope scope = SearchScope.ONELEVEL;
			switch (searchscope) {
			case BASE:
				scope = SearchScope.OBJECT;
				break;
			case ONE:
				scope = SearchScope.ONELEVEL;
				break;
			case SUB:
				scope = SearchScope.SUBTREE;
				break;
			}
			SearchRequest searchRequest = new SearchRequestImpl();
			searchRequest.setBase(new Dn(baseDn));
			searchRequest.setFilter(filter);
			searchRequest.setScope(scope);
			searchRequest.addAttributes(attributes);
			return searchRequest;
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

}
