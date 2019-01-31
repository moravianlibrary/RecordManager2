package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum BiblioLinkerSimilarType implements StringValueEnum {

	AUTH("auth"),
	AUTH_CONSPECTUS("auth+conspectus"),
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
