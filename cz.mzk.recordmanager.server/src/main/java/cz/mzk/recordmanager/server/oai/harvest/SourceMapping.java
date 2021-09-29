package cz.mzk.recordmanager.server.oai.harvest;

import cz.mzk.recordmanager.server.model.ImportConfiguration;

public class SourceMapping {

	private ImportConfiguration importConfiguration;
	private String tag;
	private char subfield;
	private String value;

	public SourceMapping(ImportConfiguration importConfiguration, String tag, char subfield, String value) {
		this.importConfiguration = importConfiguration;
		this.tag = tag;
		this.subfield = subfield;
		this.value = value;
	}

	public ImportConfiguration getImportConfiguration() {
		return importConfiguration;
	}

	public void setImportConfiguration(ImportConfiguration importConfiguration) {
		this.importConfiguration = importConfiguration;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public char getSubfield() {
		return subfield;
	}

	public void setSubfield(char subfield) {
		this.subfield = subfield;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "SourceMapping{" +
				"importConfiguration=" + importConfiguration +
				", tag='" + tag + '\'' +
				", subfield=" + subfield +
				", value='" + value + '\'' +
				'}';
	}

}