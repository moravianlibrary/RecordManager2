package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAIGetRecord {

	@XmlElement(name="record")
	private OAIRecord record;
	
	public OAIRecord getRecord() {
		return record;
	}
}
