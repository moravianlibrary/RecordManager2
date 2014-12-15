package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAIRequest {

	@XmlAttribute(name = "resumptionToken")
	private String resumptionToken;

	@XmlAttribute(name = "verb")
	private String verb;

	@XmlValue
	private String oaiUrl;

	public String getResumptionToken() {
		return resumptionToken;
	}

	public void setResumptionToken(String resumptionToken) {
		this.resumptionToken = resumptionToken;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getOaiUrl() {
		return oaiUrl;
	}

	public void setOaiUrl(String oaiUrl) {
		this.oaiUrl = oaiUrl;
	}

	public String toString() {
		return String.format(
				"OAIRequst[resumptionToken='%s', verb='%s', oaiUrl='%s'",
				resumptionToken, verb, oaiUrl);
	}

}
