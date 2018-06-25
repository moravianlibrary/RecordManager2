package cz.mzk.recordmanager.server.imports.manuscriptorium;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ApacheHttpClient;

public class ManuscriptoriumFulltextWriter implements
		ItemWriter<HarvestedRecordUniqueId>, StepExecutionListener {

	private static Logger logger = LoggerFactory
			.getLogger(ManuscriptoriumFulltextWriter.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	protected SessionFactory sessionFactory;

	private static String FULLTEXT_URL = "http://dbase.aipberoun.cz/manu3/oai/?verb=GetRecord&metadataPrefix=tei&identifier=";

	private ManuscriptoriumFulltextXmlStreamReader fulltextReader;

	private InputStream teiReader;

	private ApacheHttpClient client;

	private DocumentBuilder documentBuilder;

	private Transformer transformer;

	private static final String TEI = "TEI";

	@Override
	public void write(List<? extends HarvestedRecordUniqueId> items)
			throws Exception {
		for (HarvestedRecordUniqueId uniqueId : items) {
			HarvestedRecord hr = harvestedRecordDao.get(uniqueId);
			if (!hr.getFulltextKramerius().isEmpty())
				continue;
			getNextFulltext(uniqueId.getRecordId());
			FulltextKramerius fk = new FulltextKramerius();
			String fulltext = fulltextReader.next();
			if (fulltext.isEmpty()) {
				logger.warn("Fulltext from " + FULLTEXT_URL
						+ uniqueId.getRecordId() + " is empty.");
			} else {
				fk.setFulltext(fulltext.getBytes());
				fk.setUuidPage(uniqueId.getRecordId());
				fk.setPage("1");
				fk.setOrder(1L);
				hr.setFulltextKramerius(Collections.singletonList(fk));
				hr.setUpdated(new Date());

				InputStream is = new ByteArrayInputStream(hr.getRawRecord());
				Document doc = documentBuilder.parse(removeFormating(is));
				// remove old TEI element from DC
				NodeList tei = doc.getElementsByTagName(TEI);
				if (tei != null && tei.getLength() > 0) {
					Node remove = tei.item(0);
					remove.getParentNode().removeChild(tei.item(0));
				}
				// get new TEI element from source document
				Document teiDoc = documentBuilder
						.parse(removeFormating(teiReader));
				Node newNode = teiDoc.getElementsByTagName(TEI).item(0)
						.cloneNode(true);
				doc.adoptNode(newNode);
				// add TEI elemenet to DC
				Node root = doc.getFirstChild();
				root.appendChild(newNode);

				DOMSource source = new DOMSource(doc.getDocumentElement());
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				StreamResult result = new StreamResult(bos);
				transformer.transform(source, result);

				hr.setRawRecord(bos.toByteArray());
				harvestedRecordDao.persist(hr);
			}
			client.close();
		}

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

	}

	protected void getNextFulltext(String recordId) throws InterruptedException {
		try {
			logger.info("Harvesting fulltext from " + FULLTEXT_URL + recordId);
			client = new ApacheHttpClient();
			InputStream is = client.executeGet(FULLTEXT_URL + recordId);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.copy(is, output);
			teiReader = new ByteArrayInputStream(output.toByteArray());
			fulltextReader = new ManuscriptoriumFulltextXmlStreamReader(
					new ByteArrayInputStream(output.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
		} catch (Exception e) {
			throw new RuntimeException("XML parser error " + e);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	protected InputStream removeFormating(InputStream is) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null)
			sb.append(line.trim());

		return new ByteArrayInputStream(sb.toString().getBytes(
				StandardCharsets.UTF_8));
	}

}
