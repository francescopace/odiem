package org.odiem.stacks.apache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.ldap.client.api.message.AddRequest;
import org.apache.directory.ldap.client.api.message.ModifyRequest;
import org.apache.directory.ldap.client.api.message.SearchRequest;
import org.apache.directory.ldap.client.api.message.SearchResponse;
import org.apache.directory.ldap.client.api.message.SearchResultEntry;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.ModificationOperation;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.entry.client.DefaultClientAttribute;
import org.apache.directory.shared.ldap.entry.client.DefaultClientEntry;
import org.apache.directory.shared.ldap.filter.SearchScope;
import org.apache.directory.shared.ldap.message.control.Control;
import org.apache.directory.shared.ldap.name.DN;
import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;

public class Converter {

	public static OdmAttribute[] entryToOdmAttributes(Entry entry) {
		OdmAttribute[] tmp = null;
		if (entry != null) {
			tmp = new OdmAttribute[entry.size()];
			Iterator<EntryAttribute> it = entry.iterator();
			int i = 0;
			while (it.hasNext()) {
				EntryAttribute entryAttribute = it.next();
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

		DefaultClientEntry tmp = new DefaultClientEntry(new DN(dn));
		if (attributes != null) {
			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					tmp.add(odmAttribute.getName(), odmAttribute.getValues());
				}
			}
		}
		AddRequest addRequest = new AddRequest(tmp);
		addRequest.add(controls);

		return addRequest;
	}

	public static ModifyRequest createModifyRequest(String dn,
			List<OdmAttribute> attributes, Control... controls)
			throws Exception {
		ModifyRequest tmp = new ModifyRequest(new DN(dn));
		tmp.add(controls);
		if (attributes != null) {
			for (OdmAttribute odmAttribute : attributes) {
				EntryAttribute entry = new DefaultClientAttribute(
						odmAttribute.getName(), odmAttribute.getValues());
				if (odmAttribute.getValues().length > 0) {
					tmp.addModification(entry,
							ModificationOperation.REPLACE_ATTRIBUTE);
				} else {
					tmp.addModification(entry,
							ModificationOperation.REMOVE_ATTRIBUTE);
				}
			}
		}
		return tmp;
	}

	public static List<OdmSearchResultEntry> searchResponseToOdmSearchResultEntry(
			Cursor<SearchResponse> cursor) throws Exception {

		ArrayList<OdmSearchResultEntry> list = new ArrayList<OdmSearchResultEntry>();

		while (cursor.next()) {
			SearchResultEntry searchResultEntry = (SearchResultEntry) cursor
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
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.setBaseDn(baseDn);
			searchRequest.setFilter(filter);
			searchRequest.setScope(scope);
			searchRequest.addAttributes(attributes);
			return searchRequest;
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

}
