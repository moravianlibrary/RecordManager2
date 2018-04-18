package cz.mzk.recordmanager.server.metadata.view;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;

import java.util.ArrayList;
import java.util.List;

public enum ViewTypeEnum {
	IREL("irel"),
	TECH("tech");

	private String value;

	ViewTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	/**
	 * @param metadataRecord {@link MetadataRecord} of actual record
	 * @return {@link List} of possible {@link ViewTypeEnum} values for input {@link MetadataRecord}
	 */
	public static List<String> getPossibleValues(final MetadataRecord metadataRecord) {
		List<String> results = new ArrayList<>();
		for (ViewTypeEnum viewType : ViewTypeEnum.values()) {
			switch (viewType) {
			case IREL:
				if (metadataRecord.isIrelView()) results.add(viewType.getValue());
				break;
			case TECH:
				if (metadataRecord.isTechView()) results.add(viewType.getValue());
				break;
			default:
				results.add(viewType.getValue());
			}
		}
		return results;
	}

}
