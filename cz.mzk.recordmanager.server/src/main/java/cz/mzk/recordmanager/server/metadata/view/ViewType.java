package cz.mzk.recordmanager.server.metadata.view;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;

import java.util.ArrayList;
import java.util.List;

public enum ViewType {
	IREL("irel") {
		@Override
		protected boolean match(final MetadataRecord mr) {
			return !mr.isBlindBraille() && !mr.isMusicalScores() && !mr.isVisualDocument();
		}
	},
	TECH("tech") {
		@Override
		protected boolean match(final MetadataRecord mr) {
			return !mr.isBlindBraille() && !mr.isMusicalScores();
		}
	};

	private String value;

	ViewType(String value) {
		this.value = value;
	}

	/**
	 * @param metadataRecord {@link MetadataRecord} of actual record
	 * @return {@link List} of possible {@link ViewType} values for input {@link MetadataRecord}
	 */
	public static List<String> getPossibleValues(final MetadataRecord metadataRecord) {
		List<String> results = new ArrayList<>();
		for (ViewType viewType : ViewType.values()) {
			if (viewType.match(metadataRecord)) results.add(viewType.getValue());
		}
		return results;
	}

	public String getValue() {
		return value;
	}

	protected abstract boolean match(final MetadataRecord mr);

}
