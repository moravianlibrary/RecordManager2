package cz.mzk.recordmanager.server.marc.marc4j.upv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "pat:MainCPC")
@XmlAccessorType(XmlAccessType.FIELD)
public class MainCPC {

	@XmlElement(name = "pat:CPCClassification", required = true)
	private CPCClasification cpcClasification;

	public MainCPC() {
	}

	public CPCClasification getCpcClasification() {
		return cpcClasification;
	}

	public void setCpcClasification(CPCClasification cpcClasification) {
		this.cpcClasification = cpcClasification;
	}

	@Override
	public String toString() {
		return "MainCPC{" + "cpcClasification=" + cpcClasification + '}';
	}
}
