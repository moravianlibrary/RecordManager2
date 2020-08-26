package cz.mzk.recordmanager.server.kramerius.harvest;

public enum KrameriusHarvesterEnum {

	SORTING("sorting"),
	NOSORTING("nosorting"),
	FILE("file"),
	EMPTY("empty");

	private final String harvester;

	KrameriusHarvesterEnum(String harvester) {
		this.harvester = harvester;
	}

	public static KrameriusHarvesterEnum stringToHarvesterEnum(String strType) {
		for (KrameriusHarvesterEnum value : KrameriusHarvesterEnum.values()) {
			if (value.harvester.equals(strType)) return value;
		}
		return EMPTY;
	}
}
