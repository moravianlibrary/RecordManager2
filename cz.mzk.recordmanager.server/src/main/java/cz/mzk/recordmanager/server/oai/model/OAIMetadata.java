package cz.mzk.recordmanager.server.oai.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;

import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAIMetadata {

	@XmlAnyElement
	private Element element;

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}
	
}
