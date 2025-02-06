package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "pat:ApplicantBag")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicantBag {

	@XmlElement(name = "pat:Applicant", required = true)
	private List<Applicant> applicants;

	public ApplicantBag() {
	}

	public List<Applicant> getApplicants() {
		return applicants;
	}

	public void setApplicants(List<Applicant> applicants) {
		this.applicants = applicants;
	}

	@Override
	public String toString() {
		return "ApplicantBag{" + "applicants=" + applicants + '}';
	}
}
