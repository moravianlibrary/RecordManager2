package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAIIdentify {

	@XmlElement(name = "granularity", required = true)
	private String granularity;

	@XmlElement(name = "deletedRecord", required = true)
	private String deletedRecord;

	public OAIIdentify() {
		super();
	}

	public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

	public String getDeletedRecord() {
		return deletedRecord;
	}

	public void setDeletedRecord(String deletedRecord) {
		this.deletedRecord = deletedRecord;
	}

	@Override
	public String toString() {
		return "OAIIdentify [granularity=" + granularity + ", deletedRecord="
				+ deletedRecord + "]";
	}

}
