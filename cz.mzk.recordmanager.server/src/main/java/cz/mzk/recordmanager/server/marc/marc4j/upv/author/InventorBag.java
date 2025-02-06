package cz.mzk.recordmanager.server.marc.marc4j.upv.author;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "pat:InventorBag")
@XmlAccessorType(XmlAccessType.FIELD)
public class InventorBag {

	@XmlElement(name = "pat:Inventor", required = true)
	private List<Inventor> inventors;

	public InventorBag() {
	}

	public List<Inventor> getInventors() {
		return inventors;
	}

	public void setInventors(List<Inventor> inventors) {
		this.inventors = inventors;
	}

	@Override
	public String toString() {
		return "InventorBag{" +
				"inventors=" + inventors +
				'}';
	}
}
