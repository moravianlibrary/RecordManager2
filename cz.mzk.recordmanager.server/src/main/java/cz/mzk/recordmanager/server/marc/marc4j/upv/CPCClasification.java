package cz.mzk.recordmanager.server.marc.marc4j.upv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class CPCClasification {

	@XmlElement(name = "pat:CPCSection", required = true)
	private String cpcSection;

	@XmlElement(name = "pat:Class", required = true)
	private String cpcClass;

	@XmlElement(name = "pat:Subclass", required = true)
	private String cpcSubclass;

	@XmlElement(name = "pat:MainGroup", required = true)
	private String cpcMainGroup;

	@XmlElement(name = "pat:Subgroup", required = true)
	private String cpcSubgroup;

	public CPCClasification() {
	}

	@Override
	public String toString() {
		return "CPCClasification{" + "cpcSection='" + cpcSection + '\'' + ", cpcClass='" + cpcClass + '\'' + '}';
	}

	public String get024a() {
		return String.format("%s%s%s %s/%s", cpcSection, cpcClass, cpcSubclass, cpcMainGroup, cpcSubgroup);
	}

	public String get072Key() {
		return String.format("%s%s%s", cpcSection, cpcClass, cpcSubclass);
	}

}
