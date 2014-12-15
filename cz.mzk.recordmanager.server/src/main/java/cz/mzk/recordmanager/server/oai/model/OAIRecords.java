package cz.mzk.recordmanager.server.oai.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAIRecords {

	@XmlElement(name="record")
	private List<OAIRecord> records = new ArrayList<OAIRecord>();
	
	@XmlElement(name="resumptionToken")
	private String nextResumptionToken;

	public List<OAIRecord> getRecords() {
		return records;
	}

	public String getNextResumptionToken() {
		return nextResumptionToken;
	}

	public void setNextResumptionToken(String nextResumptionToken) {
		this.nextResumptionToken = nextResumptionToken;
	}
	
}
