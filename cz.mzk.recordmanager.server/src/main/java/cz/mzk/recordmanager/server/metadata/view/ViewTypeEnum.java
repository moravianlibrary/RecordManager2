package cz.mzk.recordmanager.server.metadata.view;

public enum ViewTypeEnum {
	IREL("irel");

	private String value;

	ViewTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
