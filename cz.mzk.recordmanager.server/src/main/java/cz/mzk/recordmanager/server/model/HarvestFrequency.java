package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.CharValueEnum;

public enum HarvestFrequency implements CharValueEnum {

	DAILY('D'),
	WEEKLY('W'),
	MONTHLY('M'),
	UNSPECIFIED('U');

	private final char value;

	HarvestFrequency(char value) {
		this.value = value;
	}

	public char getValue() {
		return value;
	}

	public static HarvestFrequency fromValue(char val) {
		for (HarvestFrequency freq : HarvestFrequency.values()) {
			if (freq.getValue() == val) {
				return freq;
			}
		}
		throw new IllegalArgumentException(String.format(
				"HarvestFrequency of value '%s' does not exist.", val));
	}

}
