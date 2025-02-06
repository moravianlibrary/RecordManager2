package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "com:PersonStructuredName")
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonStructuredName {

	@XmlElement(name = "com:FirstName", required = true)
	private String firstName;

	@XmlElement(name = "com:LastName", required = true)
	private String lastName;

	public String getNameForMarc() {
		if (lastName != null && firstName != null) {
			return lastName + ", " + firstName;
		}
		if (lastName != null) {
			return lastName;
		}
		return firstName;
	}

	public PersonStructuredName() {
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "PersonStructuredName{" +
				"firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				'}';
	}
}
