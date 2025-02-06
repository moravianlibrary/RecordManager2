package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PersonName {

	@XmlElement(name = "com:PersonStructuredName", required = true)
	private PersonStructuredName personStructuredName;

	public PersonName() {
	}

	public PersonStructuredName getPersonStructuredName() {
		return personStructuredName;
	}

	public void setPersonStructuredName(PersonStructuredName personStructuredName) {
		this.personStructuredName = personStructuredName;
	}

	@Override
	public String toString() {
		return "PersonName{" +
				"personStructuredName=" + personStructuredName +
				'}';
	}
}
