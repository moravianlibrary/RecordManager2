package cz.mzk.recordmanager.server.marc.marc4j;

import java.util.HashMap;

import org.marc4j.MarcException;
import org.marc4j.RecordStack;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * customized {@link MarcXmlHandler} implementation, only used marcFactory changed
 *
 */
public class MarcXmlHandler extends org.marc4j.MarcXmlHandler {

	private RecordStack queue;

	private StringBuffer sb;

	private Subfield subfield;

	private ControlField controlField;

	private DataField dataField;

	private Record record;

	private String tag;

	/** Constants representing each valid tag type */
	private static final int COLLECTION_ID = 1;

	private static final int LEADER_ID = 2;

	private static final int RECORD_ID = 3;

	private static final int CONTROLFIELD_ID = 4;

	private static final int DATAFIELD_ID = 5;

	private static final int SUBFIELD_ID = 6;

	/** The tag attribute name string */
	private static final String TAG_ATTR = "tag";

	/** The code attribute name string */
	private static final String CODE_ATTR = "code";

	/** The first indicator attribute name string */
	private static final String IND_1_ATTR = "ind1";

	/** The second indicator attribute name string */
	private static final String IND_2_ATTR = "ind2";

	/** Hashset for mapping of element strings to constants (Integer) */
	private static final HashMap<String, Integer> elementMap;

	private MarcFactory factory = null;

	static {
		elementMap = new HashMap<String, Integer>();
		elementMap.put("collection", new Integer(COLLECTION_ID));
		elementMap.put("leader", new Integer(LEADER_ID));
		elementMap.put("record", new Integer(RECORD_ID));
		elementMap.put("controlfield", new Integer(CONTROLFIELD_ID));
		elementMap.put("datafield", new Integer(DATAFIELD_ID));
		elementMap.put("subfield", new Integer(SUBFIELD_ID));
	}

	
	/**
	 * Default constructor.
	 * 
	 * @param queue
	 */
	public MarcXmlHandler(RecordStack queue) {
		super(queue);
		this.queue = queue;
		this.factory = MarcFactoryImpl.newInstance();
	}


	/**
	 * An event fired at the start of an element.
	 */
	public void startElement(String uri, String name, String qName,
			Attributes atts) throws SAXException {

		String realname = (name.length() == 0) ? qName : name;
		Integer elementType = (Integer) elementMap.get(realname);

		if (elementType == null) {
			return;
		}

		switch (elementType.intValue()) {
		case COLLECTION_ID:
			break;
		case RECORD_ID:
			record = factory.newRecord();
			break;
		case LEADER_ID:
			sb = new StringBuffer();
			break;
		case CONTROLFIELD_ID:
			sb = new StringBuffer();
			tag = atts.getValue(TAG_ATTR);
			controlField = factory.newControlField(tag);
			break;
		case DATAFIELD_ID:
			tag = atts.getValue(TAG_ATTR);
			String ind1 = atts.getValue(IND_1_ATTR);
			String ind2 = atts.getValue(IND_2_ATTR);
			if (ind1 == null) {
				throw new MarcException("missing ind1");
			}
			if (ind2 == null) {
				throw new MarcException("missing ind2");
			}
			if (ind1.length() == 0) {
				ind1 = " ";
			}
			if (ind2.length() == 0) {
				ind2 = " ";
			}
			dataField = factory.newDataField(tag, ind1.charAt(0),
					ind2.charAt(0));
			break;
		case SUBFIELD_ID:
			sb = new StringBuffer();
			String code = atts.getValue(CODE_ATTR);
			if (code == null || code.length() == 0) {
				code = " "; // throw new
							// MarcException("missing subfield 'code' attribute");
			}
			subfield = factory.newSubfield(code.charAt(0));
		}
	}

	/**
	 * An event fired as characters are consumed.
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (sb != null) {
			sb.append(ch, start, length);
		}
	}

	/**
	 * An event fired at the end of an element.
	 * 
	 * @param uri
	 * @param name
	 * @param qName
	 */
	public void endElement(String uri, String name, String qName)
			throws SAXException {
		String realname = (name.length() == 0) ? qName : name;
		Integer elementType = (Integer) elementMap.get(realname);

		if (elementType == null) {
			return;
		}

		switch (elementType.intValue()) {
		case COLLECTION_ID:
			break;
		case RECORD_ID:
			queue.push(record);
			break;
		case LEADER_ID:
			while(sb.length() < 24) sb.append(" ");
			Leader leader = factory.newLeader(sb.toString());
			record.setLeader(leader);
			break;
		case CONTROLFIELD_ID:
			controlField.setData(sb.toString());
			record.addVariableField(controlField);
			break;
		case DATAFIELD_ID:
			record.addVariableField(dataField);
			break;
		case SUBFIELD_ID:
			subfield.setData(sb.toString());
			dataField.addSubfield(subfield);
		}

	}

	/**
	 * An event fired at the end of the document.
	 */
	public void endDocument() throws SAXException {
		queue.end();
	}
}
