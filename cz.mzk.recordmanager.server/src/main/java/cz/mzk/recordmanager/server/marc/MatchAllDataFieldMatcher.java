package cz.mzk.recordmanager.server.marc;

import org.marc4j.marc.DataField;

public enum MatchAllDataFieldMatcher implements DataFieldMatcher {

	INSTANCE;

	@Override
	public boolean matches(DataField field) {
		return true;
	}

}
