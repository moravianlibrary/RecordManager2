package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum BiblioLinkerSimilarType implements StringValueEnum {

	AUTHOR_COMMON_TITLE("author+common_title"),
	AUTHOR_TITLE("author+title"),
	AUTHOR_TITLE_AUDIO_MUSICAL_SCORE("author+title+am"),
	AUTHOR_TITLE_AUDIO_BOOK("author+title+ab"),
	AUTHOR_TITLE_VIDEO_BOOK("author+title+vb"),
	TOPIC_KEY("topic_key"),
	ENTITY("entity"),
	ISSN_SERIES("issn_series"),
	SERIES_PUBLISHE("series+publ"),
	ENTITY_LANGUAGE("entity+lang"),
	TOPICKEY_LANGUAGE("topic_key+lang"),
	ENTITY_LIBRARIES("libraries"),
	REST("rest"),
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
