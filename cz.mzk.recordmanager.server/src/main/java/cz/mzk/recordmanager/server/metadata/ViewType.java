package cz.mzk.recordmanager.server.metadata;

public enum ViewType {
	IREL("irel");

	private String value;

	ViewType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
