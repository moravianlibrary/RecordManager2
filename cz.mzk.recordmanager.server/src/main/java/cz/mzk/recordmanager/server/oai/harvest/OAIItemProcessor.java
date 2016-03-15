package cz.mzk.recordmanager.server.oai.harvest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import com.google.common.base.MoreObjects;

import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class OAIItemProcessor implements ItemProcessor<List<OAIRecord>, List<HarvestedRecord>>, StepExecutionListener {

	private static final String DEFAULT_EXTRACT_ID_PATTERN = "[^:]+:[^:]+:([^:]+)";

	private static Logger logger = LoggerFactory.getLogger(OAIItemProcessor.class);

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;

	@Autowired
	protected OAIFormatResolver formatResolver;

	@Autowired
	private HibernateSessionSynchronizer sync;
	
	@Autowired 
	private MarcInterceptorFactory marcInterceptorFactory;

	private String format;
	
	private OAIHarvestConfiguration configuration;

	private Pattern extractIdPattern;

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
			// create new record
			HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
			rec = new HarvestedRecord(id);
			rec.setHarvestedFrom(configuration);
			rec.setFormat(format);
		}
		rec.setUpdated(new Date());
		if (record.getHeader().getDatestamp() != null) {
			rec.setHarvested(record.getHeader().getDatestamp());
		}
		if (record.getHeader().getDatestamp() != null) {
			rec.setTemporalOldOaiTimestamp(rec.getOaiTimestamp());
			rec.setOaiTimestamp(record.getHeader().getDatestamp());
		}
		
		if (record.getHeader().isDeleted()) {
			rec.setDeleted(new Date());
			rec.setRawRecord(new byte[0]);
			return rec;
		} else {
			rec.setDeleted(null);
			byte[] recordContent = asByteArray(record.getMetadata().getElement());
			if (configuration.isInterceptionEnabled()) {
				MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(configuration,recordContent);
				if (interceptor != null) {
					//in case of invalid MARC is error processed later
					recordContent = interceptor.intercept();
				}
			}
			rec.setRawRecord(recordContent);
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
			String regex = MoreObjects.firstNonNull(configuration.getRegex(), DEFAULT_EXTRACT_ID_PATTERN);
			extractIdPattern = Pattern.compile(regex);
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
		Matcher matcher = extractIdPattern.matcher(oaiIdentifier);
		if (matcher.matches()) {
			String id = matcher.group(1);
			return id;
		}
		return oaiIdentifier;
	}

}
