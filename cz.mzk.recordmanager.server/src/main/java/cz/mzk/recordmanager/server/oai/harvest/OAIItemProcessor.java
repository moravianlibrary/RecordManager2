package cz.mzk.recordmanager.server.oai.harvest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import com.google.common.base.MoreObjects;

import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import cz.mzk.recordmanager.server.util.RegexpExtractor;

public class OAIItemProcessor implements ItemProcessor<List<OAIRecord>, List<HarvestedRecord>>, StepExecutionListener {

	private static final String DEFAULT_EXTRACT_ID_PATTERN = "[^:]+:[^:]+:([^:]+)";
	
	private static final String METADATA_ERROR = "metadataError";

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;
	
	@Autowired
	protected DownloadImportConfigurationDAO downloadImportConfDao;

	@Autowired
	protected OAIFormatResolver formatResolver;

	@Autowired
	private HibernateSessionSynchronizer sync;
	
	@Autowired 
	private MarcInterceptorFactory marcInterceptorFactory;

	private String format;
	
	private ImportConfiguration configuration;

	private RegexpExtractor idExtractor;

	private Transformer transformer;

	@Override
	public List<HarvestedRecord> process(List<OAIRecord> oaiRecs) throws Exception {
		List<HarvestedRecord> result = new ArrayList<>();
		for (OAIRecord oaiRec: oaiRecs) {
			HarvestedRecord rec = createHarvestedRecord(oaiRec);
			if (rec != null) {
				result.add(rec);
			}
		}
		return result;
	}

	protected HarvestedRecord createHarvestedRecord(OAIRecord record) throws TransformerException {
		String recordId = idExtractor.extract(record.getHeader().getIdentifier());
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(
				recordId, configuration);
		boolean deleted = record.getHeader().isDeleted()
				|| record.getMetadata().getElement().getTagName() == METADATA_ERROR;
		byte[] recordContent = (deleted) ? null : asByteArray(record.getMetadata().getElement());
		if (recordContent != null && configuration.isInterceptionEnabled()) {
			MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(configuration, recordContent);
			if (interceptor != null) {
				//in case of invalid MARC is error processed later
				recordContent = interceptor.intercept();
			}
		}

		if (rec == null) {
			// create new record
			HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
			rec = new HarvestedRecord(id);
			rec.setHarvestedFrom(configuration);
			rec.setFormat(format);
		} else if ((deleted && rec.getDeleted() != null && (rec.getRawRecord() == null || rec.getRawRecord().length == 0))
				|| Arrays.equals(recordContent, rec.getRawRecord())) {
			rec.setUpdated(new Date());
			rec.setShouldBeProcessed(false);
			return rec; // no change in record
		}
		rec.setShouldBeProcessed(true);
		rec.setUpdated(new Date());
		if (record.getHeader().getDatestamp() != null) {
			rec.setHarvested(record.getHeader().getDatestamp());
		}
		if (record.getHeader().getDatestamp() != null) {
			rec.setTemporalOldOaiTimestamp(rec.getOaiTimestamp());
			rec.setOaiTimestamp(record.getHeader().getDatestamp());
		}
		
		if (deleted) {
			rec.setDeleted(new Date());
			rec.setRawRecord(new byte[0]);
			return rec;
		} else {
			rec.setDeleted(null);
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
			
			OAIHarvestConfiguration hc = configDao.get(confId);
			if (hc != null) {
				format = formatResolver.resolve(hc.getMetadataPrefix());
				String regex = MoreObjects.firstNonNull(hc.getRegex(), DEFAULT_EXTRACT_ID_PATTERN);
				configuration = hc;
				idExtractor = new RegexpExtractor(regex);
			}
			else {
				DownloadImportConfiguration dic = downloadImportConfDao.get(confId);
				if (dic != null) {
					format = formatResolver.resolve(Constants.METADATA_FORMAT_XML_MARC);
					String regex = MoreObjects.firstNonNull(dic.getRegex(), DEFAULT_EXTRACT_ID_PATTERN);
					configuration = dic;
					idExtractor = new RegexpExtractor(regex);
				}
			}
			
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

}
