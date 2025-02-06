package cz.mzk.recordmanager.server.marc.marc4j.upv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "pat:FurtherCPC")
@XmlAccessorType(XmlAccessType.FIELD)
public class FurtherCPC {

	@XmlElement(name = "pat:CPCClassification", required = true)
	private List<CPCClasification> cpcClasifications;

	public FurtherCPC() {
	}

	public List<CPCClasification> getCpcClasifications() {
		return cpcClasifications;
	}

	public void setCpcClasifications(List<CPCClasification> cpcClasifications) {
		this.cpcClasifications = cpcClasifications;
	}

	@Override
	public String toString() {
		return "FurtherCPC{" + "cpcClasifications=" + cpcClasifications + '}';
	}
}
