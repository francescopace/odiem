package org.odiem.stacks.opends;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.odiem.sdk.beans.OdmAttribute;
import org.odiem.sdk.beans.OdmSearchResultEntry;
import org.odiem.sdk.beans.OdmSearchScope;
import org.odiem.sdk.exceptions.OdmException;
import org.opends.sdk.Attribute;
import org.opends.sdk.ByteString;
import org.opends.sdk.Entry;
import org.opends.sdk.ModificationType;
import org.opends.sdk.SearchScope;
import org.opends.sdk.controls.Control;
import org.opends.sdk.ldif.ConnectionEntryReader;
import org.opends.sdk.requests.AddRequest;
import org.opends.sdk.requests.ModifyRequest;
import org.opends.sdk.requests.Requests;
import org.opends.sdk.requests.SearchRequest;
import org.opends.sdk.responses.SearchResultEntry;

public class Converter {

	public static OdmAttribute[] entryToOdmAttributes(Entry entry) {
		OdmAttribute[] tmp = null;
		if (entry != null) {
			tmp = new OdmAttribute[entry.getAttributeCount()];
			Iterator<Attribute> it = entry.getAllAttributes().iterator();
			int i = 0;
			while (it.hasNext()) {
				Attribute entryAttribute = it.next();
				Iterator<ByteString> eit = entryAttribute.iterator();
				String[] values = new String[entryAttribute.size()];

				int j = 0;
				while (eit.hasNext()) {
					values[j] = eit.next().toString();
					j++;
				}

				tmp[i] = new OdmAttribute(entryAttribute.getAttributeDescriptionAsString(), values);
				i++;
			}
		}
		return tmp;
	}

	public static AddRequest createAddRequest(String dn,
			List<OdmAttribute> attributes, Control... controls)
			throws Exception {
		
		AddRequest addRequest = Requests.newAddRequest(dn);
		for (Control control : controls) {
			addRequest.addControl(control);
		}
		
		if (attributes != null) {
			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					addRequest.addAttribute(odmAttribute.getName(), odmAttribute.getValues());
				}
			}
		}

		return addRequest;
	}

	public static ModifyRequest createModifyRequest(String dn,
			List<OdmAttribute> attributes, Control... controls)
			throws Exception {
		ModifyRequest modifyRequest = Requests.newModifyRequest(dn);
		
		for (Control control : controls) {
			modifyRequest.addControl(control);
		}
		
		if (attributes != null) {
			for (OdmAttribute odmAttribute : attributes) {
				if (odmAttribute.getValues().length > 0) {
					modifyRequest.addModification(ModificationType.REPLACE,odmAttribute.getName(), odmAttribute.getValues());
				} else {
					modifyRequest.addModification(ModificationType.DELETE,odmAttribute.getName(), odmAttribute.getValues());
				}
			}
		}
		return modifyRequest;
	}

	public static List<OdmSearchResultEntry> searchResponseToOdmSearchResultEntry(
			ConnectionEntryReader search) throws Exception {

		ArrayList<OdmSearchResultEntry> list = new ArrayList<OdmSearchResultEntry>();

		while (search.hasNext()) {
			SearchResultEntry searchResultEntry = (SearchResultEntry) search
					.readEntry();

			list.add(new OdmSearchResultEntry(searchResultEntry.getName()
					.toString(), entryToOdmAttributes(searchResultEntry)));
		}
		return list;
	}

	public static SearchRequest prepareSearchRequest(String baseDn,
			OdmSearchScope searchscope, String filter, String... attributes)
			throws OdmException {
		try {
			SearchScope scope = SearchScope.SINGLE_LEVEL;
			switch (searchscope) {
			case BASE:
				scope = SearchScope.BASE_OBJECT;
				break;
			case ONE:
				scope = SearchScope.SINGLE_LEVEL;
				break;
			case SUB:
				scope = SearchScope.WHOLE_SUBTREE;
				break;
			}
			SearchRequest searchRequest = Requests.newSearchRequest(baseDn, scope, filter, attributes);
			return searchRequest;
		} catch (Exception e) {
			throw new OdmException(e);
		}
	}

}
