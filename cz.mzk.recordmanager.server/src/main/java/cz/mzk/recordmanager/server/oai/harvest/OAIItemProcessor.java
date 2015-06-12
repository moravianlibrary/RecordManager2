package cz.mzk.recordmanager.server.oai.harvest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class OAIItemProcessor implements ItemProcessor<List<OAIRecord>, List<HarvestedRecord>>, StepExecutionListener {

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;

	@Autowired
	protected OAIFormatResolver formatResolver;

	@Autowired
	private HibernateSessionSynchronizer sync;

	private String format;
	
	private OAIHarvestConfiguration configuration;

	private Transformer transformer;
	
	@Override
	public List<HarvestedRecord> process(List<OAIRecord> arg0) throws Exception {
		List<HarvestedRecord> result = new ArrayList<>();
		for (OAIRecord oaiRec: arg0) {
			result.add(createHarvestedRecord(oaiRec));
		}
		return result;
	}
	
	protected HarvestedRecord createHarvestedRecord(OAIRecord record) throws TransformerException {
		String recordId = extractIdentifier(record.getHeader().getIdentifier());
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(
				recordId, configuration);
		if (rec == null) {
			HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
			rec = new HarvestedRecord(id);
			rec.setHarvestedFrom(configuration);
			rec.setFormat(format);
		}
		if (record.getHeader().isDeleted()) {
			rec.setDeleted(new Date());
			rec.setRawRecord(new byte[0]);
			return rec;
		} else {
			Element element = record.getMetadata().getElement();
			rec.setRawRecord(asByteArray(element));
		}

		return rec;
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
	
	protected byte[] asByteArray(Element element) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		transformer.transform(new DOMSource(element), result);
		return bos.toByteArray();
	}
	
	protected String extractIdentifier(String oaiIdentifier) {
		if (oaiIdentifier == null) {
			return null;
		}
		
		String[] parts = oaiIdentifier.split(":");
		if (parts.length == 3) {
			return parts[2];
		}
		return oaiIdentifier;
	}

}
