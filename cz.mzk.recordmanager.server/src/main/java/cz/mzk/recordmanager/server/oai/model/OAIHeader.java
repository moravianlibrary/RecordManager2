package cz.mzk.recordmanager.server.oai.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAIHeader {
	
	@XmlElement(name="identifier", required=true)
	private String identifier;
	
	@XmlElement(name="datestamp", required=true)
	private Date datestamp;
	
	@XmlElement(name="setSpec")
	private List<String> setSpecs = new ArrayList<String>();
	
	@XmlElement(name="deleted")
	private boolean deleted;
	
	@XmlAttribute(name="status")
	private String status;
	
	public OAIHeader() {	
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Date getDatestamp() {
		return datestamp;
	}

	public void setDatestamp(Date datestamp) {
		this.datestamp = datestamp;
	}

	public List<String> getSetSpecs() {
		return setSpecs;
	}

	public boolean isDeleted() {
		return status != null && status.equalsIgnoreCase("deleted");
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return String.format("OAIHeader[identifier='%s', datestamp='%s']", identifier, datestamp);
	}
	
}
