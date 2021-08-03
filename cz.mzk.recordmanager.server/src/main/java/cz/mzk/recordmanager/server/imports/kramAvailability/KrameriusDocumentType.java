package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;

public enum KrameriusDocumentType implements StringValueEnum {

	PERIODICAL("periodical"),
	PERIODICAL_ITEM("periodicalitem"),
	PERIODICAL_VOLUME("periodicalvolume"),
	PAGE("page");

	private final String value;

	KrameriusDocumentType(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	public static KrameriusDocumentType fromValue(String val) {
		for (KrameriusDocumentType type : KrameriusDocumentType.values()) {
			if (type.getValue().equals(val)) {
				return type;
			}
		}
		throw new IllegalArgumentException(String.format(
				"KrameriusDocumentType of value '%s' does not exist.", val));
	}


}
