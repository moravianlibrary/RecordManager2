package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum BiblioLinkerSimilarType implements StringValueEnum {

	AUTHOR_COMMON_TITLE("author+common_title"),
	AUTHOR_TITLE("author+title"),
	TOPIC_KEY("topic_key"),
	ENTITY("entity"),
	ISSN_SERIES("issn_series"),
	SERIES_PUBLISHE("series+publ"),
	ENTITY_LANGUAGE("entity+lang"),
	UNSPECIFIED("unspecified");

	private final String value;

	BiblioLinkerSimilarType(String name) {
		this.value = name;
	}

	@Override
	public String getValue() {
		return value;
	}

}
