package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum Mappings996Enum implements StringValueEnum {

	ALEPH("aleph"),
	CASLIN("caslin"),
	DAWINCI("dawinci"),
	DEFAULT("default"),
	KOHA("koha"),
	TRITIUS("tritius");

	private final String value;

	Mappings996Enum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
