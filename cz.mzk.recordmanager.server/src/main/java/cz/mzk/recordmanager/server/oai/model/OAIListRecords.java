package cz.mzk.recordmanager.server.oai.model;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="OAI-PMH")
@XmlAccessorType(XmlAccessType.FIELD)
public class OAIListRecords {
	
	@XmlElement(name="request")
	private OAIRequest request;
	
	@XmlElement(name = "ListRecords")	
	private OAIRecords records;
	
	public OAIRequest getRequest() {
		return request;
	}

	public void setRequest(OAIRequest request) {
		this.request = request;
	}

	public void setRecords(OAIRecords records) {
		this.records = records;
	}
	
	public List<OAIRecord> getRecords() {
		return (records != null) ? records.getRecords() : Collections.<OAIRecord>emptyList();
	}
	
	public String getNextResumptionToken() {
		return (records != null) ? records.getNextResumptionToken() : null;
	}

	public String toString() {
		return String.format("OAIListRecords[request=%s]", request);
	}
	
}
