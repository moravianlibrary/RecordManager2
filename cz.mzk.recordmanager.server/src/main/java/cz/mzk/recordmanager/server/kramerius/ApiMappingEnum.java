package cz.mzk.recordmanager.server.kramerius;

public enum ApiMappingEnum {
	API("api"),
	METADATA("metadata"),
	COLLECTION("collection"),
	MODIFIED("modified"),
	PID("pid"),
	MODEL("model"),
	DC("DC"),
	BIBLIO_MODS("BIBLIO_MODS");

	private final String value;

	ApiMappingEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
