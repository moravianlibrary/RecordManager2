package cz.mzk.recordmanager.server.imports.inspirations;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum InspirationType implements StringValueEnum {
	INSPIRATION("inspiration"),
	TOP_RESULTS("top_results");

	private final String value;

	InspirationType(String type) {
		this.value = type;
	}

	@Override
	public String getValue() {
		return value;
	}
}
