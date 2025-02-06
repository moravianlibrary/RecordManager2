package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Name {

	@XmlElement(name = "com:OrganizationName")
	private OrganizationName organizationName;

	@XmlElement(name = "com:PersonName")
	private PersonName personName;

	public Name() {
	}

	public OrganizationName getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(OrganizationName organizationName) {
		this.organizationName = organizationName;
	}

	public PersonName getPersonName() {
		return personName;
	}

	public void setPersonName(PersonName personName) {
		this.personName = personName;
	}

	@Override
	public String toString() {
		return "Name{" +
				"organizationName=" + organizationName +
				", personName=" + personName +
				'}';
	}
}
