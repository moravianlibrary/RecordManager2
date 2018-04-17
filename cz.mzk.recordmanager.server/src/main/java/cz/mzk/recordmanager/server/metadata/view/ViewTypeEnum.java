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

	public static List<String> getPossibleValues(final MetadataRecord metadataRecord) {
		List<String> results = new ArrayList<>();
		for (ViewTypeEnum viewTypeEnum : ViewTypeEnum.values()) {
			switch (viewTypeEnum) {
			case IREL:
				if (metadataRecord.isIrelView()) results.add(viewTypeEnum.getValue());
				break;
			case TECH:
				if (metadataRecord.isTechView()) results.add(viewTypeEnum.getValue());
				break;
			default:
				results.add(viewTypeEnum.getValue());
			}
		}
		return results;
	}

}
