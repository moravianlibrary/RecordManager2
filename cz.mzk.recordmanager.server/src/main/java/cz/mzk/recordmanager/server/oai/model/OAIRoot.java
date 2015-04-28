package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="OAI-PMH")
@XmlAccessorType(XmlAccessType.FIELD)
public class OAIRoot {
	
	@XmlElement(name="request")
	private OAIRequest request;
	
	@XmlElement(name = "ListRecords")	
	private OAIListRecords listRecords;
	
	@XmlElement(name = "ListIdentifiers")	
	private OAIListIdentifiers listIdentifiers;
	
	@XmlElement(name = "GetRecord")	
	private OAIGetRecord getRecord;
	
	@XmlElement(name = "Identify")	
	private OAIIdentify identify;
	
	@XmlElement(name="error")
	private String oaiError;
	
	public OAIIdentify getIdentify() {
		return identify;
	}

	public void setIdentify(OAIIdentify identify) {
		this.identify = identify;
	}

	public OAIRequest getRequest() {
		return request;
	}

	public void setRequest(OAIRequest request) {
		this.request = request;
	}

	public void setRecords(OAIListRecords records) {
		this.listRecords = records;
	}
	
	public OAIListIdentifiers getListIdentifiers() {
		return listIdentifiers;
	}
	
	public OAIGetRecord getGetRecord() {
		return getRecord;
	}
	
	public OAIListRecords getListRecords() {
		return listRecords;
	}

	public String getOaiError() {
		return oaiError;
	}

	public void setOaiError(String oaiError) {
		this.oaiError = oaiError;
	}

	public String toString() {
		return String.format("OAIListRecords[request=%s]", request);
	}
	
}
