package cz.mzk.recordmanager.server.oai.harvest;

public class SourceMapping {

	private String tag;
	private char subfield;
	private String value;

	public SourceMapping(String tag, char subfield, String value) {
		this.tag = tag;
		this.subfield = subfield;
		this.value = value;
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
		return "Values{" +
				"tag='" + tag + '\'' +
				", subfield='" + subfield + '\'' +
				", value='" + value + '\'' +
				'}';
	}

}