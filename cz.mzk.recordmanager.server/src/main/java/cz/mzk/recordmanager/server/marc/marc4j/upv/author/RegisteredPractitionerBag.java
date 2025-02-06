package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "pat:RegisteredPractitionerBag")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegisteredPractitionerBag {

	@XmlElement(name = "pat:RegisteredPractitioner", required = true)
	private List<RegisteredPractitioner> registeredPractitioners;

	public RegisteredPractitionerBag() {
	}

	public List<RegisteredPractitioner> getRegisteredPractitioners() {
		return registeredPractitioners;
	}

	public void setRegisteredPractitioners(List<RegisteredPractitioner> registeredPractitioners) {
		this.registeredPractitioners = registeredPractitioners;
	}

	@Override
	public String toString() {
		return "RegisteredPractitionerBag{" +
				"registeredPractitioners=" + registeredPractitioners +
				'}';
	}
}
