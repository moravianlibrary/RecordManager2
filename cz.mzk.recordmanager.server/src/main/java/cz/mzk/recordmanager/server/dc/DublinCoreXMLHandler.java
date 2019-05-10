package cz.mzk.recordmanager.server.dc;

//import java.util.Stack;


import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DublinCoreXMLHandler extends DefaultHandler {

	private StringBuffer sb;
	private DublinCoreRecord dcRecord;
//	private Stack<String> elementStack = new Stack<String>();
	
	private static final String DC_CONTRIBUTOR="dc:contributor";
	private static final String DC_COVERAGE="dc:coverage";
	private static final String DC_CREATOR="dc:creator";
	private static final String DC_DATE="dc:date";
	private static final String DC_DESCRIPTION="dc:description";
	private static final String DC_FORMAT="dc:format";
	private static final String DC_IDENTIFIER="dc:identifier";
	private static final String DC_LANGUAGE="dc:language";
	private static final String DC_PUBLISHER="dc:publisher";
	private static final String DC_RELATION="dc:relation";
	private static final String DC_RIGHTS="dc:rights";
	private static final String DC_SOURCE="dc:source";
	private static final String DC_SUBJECT="dc:subject";
	private static final String DC_TITLE="dc:title";
	private static final String DC_TYPE="dc:type";
	
	private static final String ESE_TYPE = "europeana:type";
	private static final String ESE_RIGHTS = "europeana:rights";
	private static final String ESE_ISSHOWNAT = "europeana:isShownAt";
	private static final String ESE_DATAPROVIDER = "europeana:dataProvider";
	
	private static final String DCTERMS_EXTENT = "dcterms:extent";
	private static final String DCTERMS_TABLE_OF_CONTENTS = "dcterms:tableOfContents";
	private static final String DCTERMS_ALTERNATIVE = "dcterms:alternative";

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
//		this.elementStack.push(qName);

		if ("oai_dc:dc".equals(qName)
				|| "record".equals(qName)
				|| "europeana:record".equals(qName)) {
			// New DC Record instance
			this.dcRecord = new DublinCoreRecordImpl();
		}
		
		sb = new StringBuffer();
		
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (sb != null) {
			String s = sb.toString();
			StringEscapeUtils.unescapeXml(s);
			
			switch (qName) {
			case DC_CONTRIBUTOR:
				this.dcRecord.addContributor(s);
				break;
			case DC_COVERAGE:
				this.dcRecord.addCoverage(s);
				break;
			case DC_CREATOR:
				this.dcRecord.addCreator(s);
				break;	
			case DC_DATE:
				this.dcRecord.addDate(s);
				break;
			case DC_DESCRIPTION:
				this.dcRecord.addDescription(s);
				break;
			case DC_FORMAT:
				this.dcRecord.addFormat(s);
				break;
			case DC_IDENTIFIER: 
				this.dcRecord.addIdentifier(s);
				break;
			case DC_LANGUAGE:
				this.dcRecord.addLanguage(s);
				break;
			case DC_PUBLISHER:
				this.dcRecord.addPublisher(s);
				break;
			case DC_RELATION:
				this.dcRecord.addRelation(s);
				break;
			case DC_RIGHTS:
				this.dcRecord.addRights(s);
				break;
			case DC_SOURCE:
				this.dcRecord.addSource(s);
				break;
			case DC_SUBJECT:
				this.dcRecord.addSubjects(s);
				break;
			case DC_TITLE: 			
				this.dcRecord.addTitle(s);
				break;
			case DC_TYPE:
				this.dcRecord.addType(s);
				break;
			case ESE_TYPE:
				this.dcRecord.addType(s);
				break;
			case ESE_RIGHTS:
				this.dcRecord.addRights(s);
				break;
			case ESE_ISSHOWNAT:
				this.dcRecord.addUrls(s);
				break;
			case ESE_DATAPROVIDER:
				this.dcRecord.addSource(s);
				break;
			case DCTERMS_ALTERNATIVE:
				this.dcRecord.addTitleAlt(s);
				break;
			case DCTERMS_EXTENT:
				this.dcRecord.addPhysical(s);
				break;
			case DCTERMS_TABLE_OF_CONTENTS:
				this.dcRecord.addContent(s);
				break;
			}
		}

	}

	/*
	 * checks element name and inputs value to correct array
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		if (sb != null) {
			sb.append(ch, start, length);
		}
		
	}


	public DublinCoreRecord getRecord() {
		//check we have any data before returning!
		if (dcRecord==null) {
			throw new InvalidDcException("InvalidDcException: dcRecord is empty!");
		}
		return this.dcRecord;
	}

}
