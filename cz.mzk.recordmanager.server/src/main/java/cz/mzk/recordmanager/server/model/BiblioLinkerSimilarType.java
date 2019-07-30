package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum BiblioLinkerSimilarType implements StringValueEnum {

	AUTH_COMMON_TITLE("auth+common_title"),
	AUTHOR_COMMON_TITLE("author+common_title"),
	AUTH_TTILE_LANG("auth+title+lang"),
	AUTHOR_TTILE_LANG("author+title+lang"),
	AUTH_TTILE("auth+title"),
	AUTHOR_TTILE("author+title"),
	TITLE_ENTITY_LANG("title+entity"),
	TITLE_ENTITY_AUTH_KEY_LANG("title+entity_auth"),
	TOPIC_KEY_LANG("topic_key+lang"),
	AUTH_KEY_FORMAT_LANG("auth+format+lang"),
	AUTHOR_FORMAT_LANG("author+format+lang"),
	ENTITY_LANG("entity+lang"),
	ENTITY_AUTH_KEY_LANG("entity_auth+lang"),
	TITLE_PLUS_LANG("title_plus+lang"),
	ISSN_SERIES("issn_series"),
	SERIES_PUBLISHE_LANG("series+publ+lang"),
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
