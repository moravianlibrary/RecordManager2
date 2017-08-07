package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class AdresarStreamReader implements MarcReader {

	private Record record;

	private MarcFactory factory;

	private XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_RECORD = "record";
	private static final String ELEMENT_FIXFIELD = "fixfield";
	private static final String ELEMENT_VARFIELD = "varfield";
	private static final String ELEMENT_SUBFIELD = "subfield";
	
	private static final String ATTR_NAME_ID = "id";
	private static final String ATTR_I1_NAME = "i1";
	private static final String ATTR_I2_NAME = "i2";
	private static final String ATTR_LABEL_NAME = "label";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public AdresarStreamReader(InputStream input) {
		xmlFactory = XMLInputFactory.newInstance();
		factory = MarcFactoryImpl.newInstance();
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
		try {
			xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			this.xmlReader = xmlFactory.createXMLStreamReader(input);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		try {
			return xmlReader.hasNext();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Record next() {
		record = null;
		DataField df = null;
		try {
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						record = factory.newRecord();
						break;
					case ELEMENT_FIXFIELD:
						if (getAttr(ATTR_NAME_ID).equalsIgnoreCase("LDR")) {
							record.setLeader(factory.newLeader(xmlReader.getElementText()));
						}
						else if (getAttr(ATTR_NAME_ID).equalsIgnoreCase("SYS")) {
							record.addVariableField(factory.newControlField("SYS", xmlReader.getElementText()));
						}
						break;
					case ELEMENT_VARFIELD:
						df = factory.newDataField(getAttr(ATTR_NAME_ID), getAttr(ATTR_I1_NAME).charAt(0), getAttr(ATTR_I2_NAME).charAt(0));
						break;
					case ELEMENT_SUBFIELD:
						if (df != null) {
							df.addSubfield(factory.newSubfield(getAttr(ATTR_LABEL_NAME).charAt(0), xmlReader.getElementText()));
						}
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						while (xmlReader.hasNext() && xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
							xmlReader.next();
						}
						return record;
					case ELEMENT_VARFIELD:
						record.addVariableField(df);
						break;
					}
					break;
				}
				xmlReader.next();

			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getAttr(String attributeName) {
		return xmlReader.getAttributeValue(null, attributeName);
	}

}
