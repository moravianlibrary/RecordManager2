package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OAI-PMH")
@XmlAccessorType(XmlAccessType.FIELD)
public class OAIIdentifyRequest {

	@XmlElement(name = "Identify")
	private OAIIdentify identify;

	public OAIIdentify getIdentify() {
		return identify;
	}

	public void setIdentify(OAIIdentify identify) {
		this.identify = identify;
	}

	@Override
	public String toString() {
		return "OAIIdentifyRequest [identify=" + identify + "]";
	}

}
