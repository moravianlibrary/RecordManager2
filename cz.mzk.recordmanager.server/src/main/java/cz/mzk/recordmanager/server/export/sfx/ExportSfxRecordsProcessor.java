package cz.mzk.recordmanager.server.export.sfx;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.constants.SfxConstants;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

public class ExportSfxRecordsProcessor implements
		ItemProcessor<DedupRecord, String>, StepExecutionListener {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private DocumentBuilder documentBuilder;

	private Transformer transformer;

	@Override
	public String process(DedupRecord dr) throws Exception {
		MergedSfxRecord sfxRec = new MergedSfxRecord();
		for (HarvestedRecord hr : harvestedRecordDao.getByDedupRecord(dr)) {
			if (hr.isDeleted()) continue;
			InputStream is = new ByteArrayInputStream(hr.getRawRecord());
			MarcRecord mr = marcXmlParser.parseRecord(is);
			sfxRec.addRecord(mr);
		}
		return sfxRec.toXmlString();
	}

	protected class MergedSfxRecord {

		private Set<String> sfxId = new HashSet<>();
		private Set<String> titles = new HashSet<>();
		private List<String> isbns = new ArrayList<>();
		private Set<String> issns = new HashSet<>();
		private Set<String> objectTypes = new HashSet<>();
		private Set<Coverage> coverages = new HashSet<>();
		Document doc;

		public void addRecord(MarcRecord mr) {
			sfxId.add(mr.getControlField("001"));
			isbns.addAll(mr.getFields("020", 'a'));
			issns.addAll(mr.getFields("022", 'a'));
			titles.addAll(mr.getFields("245", 'a'));
			titles.addAll(mr.getFields("246", 'a'));
			objectTypes.addAll(mr.getFields("300", 'a'));
			coverages.addAll(Coverage.create(mr.getDataFields("COV")));
		}

		public String toXmlString() throws TransformerException {
			doc = documentBuilder.newDocument();

			Element root = doc.createElement(SfxConstants.ELEMENT_ITEM);
			root.setAttribute(SfxConstants.ATTRIBUTE_TYPE, SfxConstants.VALUE_ELECTRONIC);
			doc.appendChild(root);

			generateElements(sfxId, SfxConstants.ELEMENT_SFX_ID);
			generateElements(titles, SfxConstants.ELEMENT_TITLE);
			generateElements(isbns, SfxConstants.ELEMENT_ISBN);
			generateElements(issns, SfxConstants.ELEMENT_ISSN);
			generateElements(objectTypes, SfxConstants.ELEMENT_OBJECT_TYPE);
			coverages = new TreeSet<>(coverages); // sort
			coverages.forEach(c -> c.addToXml(doc));

			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.getBuffer().toString();
		}

		private void generateElements(Collection<String> collection, String elementName) {
			Node root = doc.getFirstChild();
			collection.forEach(i -> {
				Element el = doc.createElement(elementName);
				el.setTextContent(i);
				root.appendChild(el);
			});
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		} catch (Exception e) {
			throw new RuntimeException("XML parser error " + e);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}
}
