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

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;

@Component
@StepScope
public class OAIItemWriter implements ItemWriter<List<OAIRecord>>,
		StepExecutionListener {
	
	private static Logger logger = LoggerFactory.getLogger(OAIItemWriter.class);

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;
	
	@Autowired
	protected OAIFormatResolver formatResolver;
	
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
					logger.warn("TransformerException when storing {}: ", record, te);
				}
			}
		}
	}

	protected void write(OAIRecord record) throws TransformerException {
		String recordId = record.getHeader().getIdentifier();
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(
				recordId, configuration);
		if (rec == null) {
			rec = new HarvestedRecord();
			rec.setOaiRecordId(record.getHeader().getIdentifier());
			rec.setHarvestedFrom(configuration);
			rec.setFormat(format);
		}
		if (record.getHeader().isDeleted()) {
			rec.setDeleted(new Date());
			rec.setRawRecord(null);
		} else {
			Element element = record.getMetadata().getElement();
			rec.setRawRecord(asByteArray(element));
		}
		recordDao.persist(rec);
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
