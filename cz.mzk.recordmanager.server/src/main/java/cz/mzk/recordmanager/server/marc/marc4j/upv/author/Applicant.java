package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Applicant {

	@XmlElement(name = "com:Contact", required = true)
	private Contact contact;

	public Applicant() {
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	@Override
	public String toString() {
		return "Applicant{" +
				"contact=" + contact +
				'}';
	}
}
