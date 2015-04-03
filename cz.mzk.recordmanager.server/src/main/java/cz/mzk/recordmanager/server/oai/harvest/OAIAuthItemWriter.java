package cz.mzk.recordmanager.server.oai.harvest;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.AuthorityRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class OAIAuthItemWriter implements ItemWriter<List<OAIRecord>>,
		StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(OAIAuthItemWriter.class);

	@Autowired
	protected AuthorityRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;

	@Autowired
	protected OAIFormatResolver formatResolver;

	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	@Autowired
	private HibernateSessionSynchronizer sync;

	private String format;

	private OAIHarvestConfiguration configuration;

	private Transformer transformer;

	@Override
	public void write(List<? extends List<OAIRecord>> items) throws Exception {
		for (List<OAIRecord> records : items) {
			for (OAIRecord record : records) {
				try {
					write(record);
				} catch (TransformerException te) {
					logger.warn("TransformerException when storing {}: ",
							record, te);
				}
			}
		}
	}

	protected void write(OAIRecord record) throws TransformerException {
		String recordId = record.getHeader().getIdentifier();
		AuthorityRecord authRec = recordDao.findByIdAndHarvestConfiguration(
				recordId, configuration);
		if (authRec == null) {
			authRec = new AuthorityRecord();
			authRec.setOaiRecordId(record.getHeader().getIdentifier());
			authRec.setHarvestedFrom(configuration);
			authRec.setFormat(format);
		}
		if (record.getHeader().isDeleted()) {
			authRec.setDeleted(new Date());
			authRec.setRawRecord(new byte[0]);
			recordDao.persist(authRec);
			return;
		} else {
			Element element = record.getMetadata().getElement();
			authRec.setRawRecord(asByteArray(element));
		}
		
		recordDao.persist(authRec);
	}

	protected byte[] asByteArray(Element element) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		transformer.transform(new DOMSource(element), result);
		return bos.toByteArray();
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder session = sync.register()) {
			Long confId = stepExecution.getJobParameters().getLong(
					"configurationId");
			configuration = configDao.get(confId);
			format = formatResolver.resolve(configuration.getMetadataPrefix());
			try {
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException tce) {
				throw new RuntimeException(tce);
			}
		}
	}

}
