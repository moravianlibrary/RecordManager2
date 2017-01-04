package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.ctc.wstx.exc.WstxEOFException;
import com.ctc.wstx.exc.WstxParsingException;

public class ZakonyProLidiFulltextXmlStreamReader {
    
    private XMLInputFactory xmlFactory;
	
    private XMLEventReader eventReader;
    
    private static final String ELEMENT_DIV = "div";
    private static final String ELEMENT_P = "p";
    private static final String ELEMENT_H3 = "h3";
    
    private static final String ATTR_NAME_CLASS = "class";
    
    private static final String ATTR_VALUE_FRAGS_NOVEL = "Frags NOVEL";
    
    /**
     * Constructs an instance with the specified input stream.
     */
    public ZakonyProLidiFulltextXmlStreamReader(InputStream input) {
    	xmlFactory = XMLInputFactory.newInstance();
        initializeReader(input);
    }

    private void initializeReader(InputStream input){
    	try {
        	xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			this.eventReader = xmlFactory.createXMLEventReader(input);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
    }
	
	public String next() {
		boolean fulltext = false;
		String result = "";
		int countDiv = 0;
		while (eventReader.hasNext()) {
			try {
				XMLEvent xmlEvent = eventReader.nextEvent();
				
			    if (xmlEvent.isStartElement()) {
			        switch (xmlEvent.asStartElement().getName().getLocalPart()) {
			        case ELEMENT_DIV:
			        	if (isAttribute(xmlEvent.asStartElement(), ATTR_NAME_CLASS, ATTR_VALUE_FRAGS_NOVEL)) {
			        		fulltext = true;
			        	}
			        	if (fulltext) ++countDiv;
			        	break;
			        case ELEMENT_H3:
					case ELEMENT_P:
						if(fulltext){
							String xmlFragment = readElementBody(eventReader);
				            result += xmlFragment + "\n";
						}
						break;
					default:
						break;
					} 
			    } else if (xmlEvent.isEndElement()) {
			    	switch (xmlEvent.asEndElement().getName().getLocalPart()) {
					case ELEMENT_DIV:
						if (fulltext) --countDiv;
						if (countDiv == 0) fulltext = false;
						break;
					default:
						break;
					}
			    }
			} catch (WstxEOFException e) {
				break;
			} catch (WstxParsingException e) {
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	protected String readElementBody(XMLEventReader eventReader) throws XMLStreamException {
		StringWriter buf = new StringWriter();

		int depth = 0;
		while (eventReader.hasNext()) {
			// peek event
			XMLEvent xmlEvent = eventReader.peek();
	
			if (xmlEvent.isStartElement()) ++depth;
			else if (xmlEvent.isEndElement()) {
				--depth;
				// reached END_ELEMENT tag?
				// break loop, leave event in stream
				if (depth < 0) break;
			}
	
			xmlEvent = eventReader.nextEvent();
			xmlEvent.writeAsEncodedUnicode(buf);
		}
		String result = buf.getBuffer().toString();
		result = result.replaceAll("<[^>]*>", "");
		result = result.replaceAll("^[\\s§0-9]*$", "");

		return result;
	}
	
	protected boolean isAttribute(StartElement element, String name, String value) {
		Iterator<?> iterator = element.getAttributes();
    	while (iterator.hasNext()) {
    		Attribute attribute = (Attribute) iterator.next();
    		if (attribute.getName().getLocalPart().equals(name) && attribute.getValue().equals(value)) {
    			return true;
    		}
    	}
    	return false;
	}
}
