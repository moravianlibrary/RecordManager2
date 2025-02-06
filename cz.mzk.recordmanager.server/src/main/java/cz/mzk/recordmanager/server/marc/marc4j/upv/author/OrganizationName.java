package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrganizationName {

	@XmlElement(name = "com:OrganizationStandardName", required = true)
	private String organizationStandardName;

	public OrganizationName() {
	}

	public String getOrganizationStandardName() {
		return organizationStandardName;
	}

	public void setOrganizationStandardName(String organizationStandardName) {
		this.organizationStandardName = organizationStandardName;
	}

	@Override
	public String toString() {
		return "OrganizationName{" + "organizationStandardName='" + organizationStandardName + '\'' + '}';
	}
}
