package org.odiem.sdk.beans;

import java.util.Collections;
import java.util.List;

public class OdmSearchResult extends OdmResult {

	private List<?> results;

	public OdmSearchResult(String baseDn, List<Object> pojos) {
		super(baseDn);
		results = pojos;
	}

	public OdmSearchResult(String baseDn, List<Object> pojos,
			long executionTime, long stackExecutionTime) {
		this(baseDn, pojos);
		setExecutionTime(executionTime);
		setStackExecutionTime(stackExecutionTime);
	}

	public int getEntryCount() {
		int size = 0;
		if (results != null) {
			size = results.size();
		}
		return size;
	}

	public List<?> getSearchEntries() {
		if (results == null) {
			return null;
		} else {
			return Collections.unmodifiableList(results);
		}
	}
}
