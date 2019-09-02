package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum BiblioLinkerSimilarType implements StringValueEnum {

	AUTH_COMMON_TITLE("auth+common_title"),
	AUTHOR_COMMON_TITLE("author+common_title"),
	AUTH_TTILE_LANG("auth+title+lang"),
	AUTHOR_TTILE_LANG("author+title+lang"),
	AUTH_TTILE("auth+title"),
	AUTHOR_TITLE("author+title"),
	TITLE_ENTITY_LANG("title+entity"),
	TITLE_ENTITY_AUTH_KEY_LANG("title+entity_auth"),
	TOPIC_KEY("topic_key"),
	AUTH_KEY_FORMAT_LANG("auth+format+lang"),
	AUTHOR_FORMAT_LANG("author+format+lang"),
	ENTITY("entity"),
	ENTITY_AUTH_KEY_LANG("entity_auth+lang"),
	TITLE_PLUS_LANG("title_plus+lang"),
	ISSN_SERIES("issn_series"),
	SERIES_PUBLISHE("series+publ"),
	SOURCE_INFO_X_TOPIC_KEY("sinfox+topic_key"),
	SOURCE_INFO_T_TOPIC_KEY("sinfot+topic_key"),
	CONSPECTUS("conspectus"),
	AUTH("authority"),
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
