package cz.mzk.recordmanager.server.imports.kramAvailability;

public enum KramAvailabilityHarvestType {

	PAGES("pages"),
	TITLES("titles");

	private final String value;

	KramAvailabilityHarvestType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static KramAvailabilityHarvestType fromValue(String val) {
		for (KramAvailabilityHarvestType type : KramAvailabilityHarvestType.values()) {
			if (type.getValue().equals(val)) {
				return type;
			}
		}
		throw new IllegalArgumentException(String.format(
				"KramAvailabilityHarvestType of value '%s' does not exist.", val));
	}


}
