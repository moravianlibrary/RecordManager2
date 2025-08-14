package cz.mzk.recordmanager.server.kramerius;

public enum ApiMappingEnum {
	API("api"),
	METADATA("metadata"),
	COLLECTION("collection"),
	MODIFIED("modified"),
	PID("pid"),
	MODEL("model"),
	ACCESSIBILITY("accessibility"),
	DNNT_LABELS("dnnt_labels"),
	LICENSES("licenses"),
	AVAILABILITY_URL("availability_url"),
	DC("DC"),
	BIBLIO_MODS("BIBLIO_MODS"),
	PAGE_NUMBER("page_number"),
	PAGE_ORDER("page_order"),
	ROOT_PID("root_pid"),
	PARENT_PID("parent_pid");

	private final String value;

	ApiMappingEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
