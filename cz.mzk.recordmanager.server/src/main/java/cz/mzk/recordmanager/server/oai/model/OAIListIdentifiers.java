package cz.mzk.recordmanager.server.oai.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
public class OAIListIdentifiers {
	
	@XmlElement(name="header")
	private List<OAIHeader> headers = new ArrayList<OAIHeader>();
	
	@XmlElement(name="resumptionToken")
	private String nextResumptionToken;
	
	public List<OAIHeader> getHeaders() {
		return headers;
	}

	public String getNextResumptionToken() {
		return nextResumptionToken;
	}

	public void setNextResumptionToken(String nextResumptionToken) {
		this.nextResumptionToken = nextResumptionToken;
	}
}
