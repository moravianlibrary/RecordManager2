package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum BiblioLinkerSimilarType implements StringValueEnum {

	AUTH_COMMON_TITLE("auth+common_title"),
	AUTHOR_COMMON_TITLE("author+common_title"),
	AUTH_TTILE_LANG("auth+title+lang"),
	AUTHOR_TTILE_LANG("author+title+lang"),
	AUTH_TTILE("auth+title"),
	AUTHOR_TTILE("author+title"),
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
