package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="record", namespace = "http://www.openarchives.org/OAI/2.0/")
@XmlAccessorType(XmlAccessType.FIELD)
public class OAIRecord {
	
	@XmlElement(name="header", required=true)
	private OAIHeader header;
	
	@XmlElement(name="metadata", required=true)
	private OAIMetadata metadata;
	
	public OAIRecord() {
	}

	public OAIRecord(OAIHeader header, OAIMetadata metadata) {
		super();
		this.header = header;
		this.metadata = metadata;
	}

	public OAIHeader getHeader() {
		return header;
	}

	public void setHeader(OAIHeader header) {
		this.header = header;
	}

	public OAIMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(OAIMetadata metadata) {
		this.metadata = metadata;
	}
	
	public String toString() {
		return String.format("OAIRecord[header=%s]", header);
	}

}
