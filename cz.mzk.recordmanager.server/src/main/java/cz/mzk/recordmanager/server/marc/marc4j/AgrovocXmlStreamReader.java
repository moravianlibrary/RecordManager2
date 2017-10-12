package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class AgrovocXmlStreamReader {

	private XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String ELEMENT_RECORD = "Description";
	private static final String ELEMENT_PREFLABEL = "prefLabel";
	private static final String ELEMENT_ALTLABEL = "altLabel";
	
	private static final Pattern PATTERN_ID = Pattern.compile(".*/([^/]*)");
	
	/**
	 * Constructs an instance with the specified input stream.
	 */
	public AgrovocXmlStreamReader(InputStream input) {
		xmlFactory = XMLInputFactory.newInstance();
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

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext() {
		try {
			return xmlReader.hasNext();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns the next record in the iteration.
	 * 
	 * @return Record - the record object
	 */
	public Map<String, Map<String, List<String>>> next() {
		try {
			String id = null;
			Matcher matcher = null;
			Map<String, Map<String, List<String>>> result = new HashMap<>();
			Map<String, List<String>> innerMap = new HashMap<>();
			List<String> innerList = new ArrayList<>();
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					String tag = xmlReader.getLocalName();
					switch (tag) {
					case ELEMENT_RECORD:
						String about = xmlReader.getAttributeValue(0);
						matcher = PATTERN_ID.matcher(about);
						if (matcher.matches()) {
							id = matcher.group(1);
						}
						break;
					case ELEMENT_PREFLABEL:
					case ELEMENT_ALTLABEL:
						if (xmlReader.getAttributeValue(0).equals("cs")) {
							innerList.add(xmlReader.getElementText());
							innerMap.put(id, innerList);
							result.put(tag, innerMap);
						}
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case ELEMENT_RECORD:
						if (!result.isEmpty()) return result;
						id = null;
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

}
