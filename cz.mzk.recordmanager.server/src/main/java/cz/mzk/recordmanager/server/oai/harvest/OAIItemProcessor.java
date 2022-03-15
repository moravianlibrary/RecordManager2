package cz.mzk.recordmanager.server.oai.harvest;

import com.google.common.base.MoreObjects;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.model.*;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationMappingFieldDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import cz.mzk.recordmanager.server.util.RegexpExtractor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.Map.Entry;

public class OAIItemProcessor implements ItemProcessor<List<OAIRecord>, List<HarvestedRecord>>, StepExecutionListener {

	private static final String DEFAULT_EXTRACT_ID_PATTERN = "[^:]+:[^:]+:([^:]+)";
	
	protected static final String METADATA_ERROR = "metadataError";

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;

	@Autowired
	protected DownloadImportConfigurationDAO downloadImportConfDao;

	@Autowired
	protected ImportConfigurationMappingFieldDAO importConfigurationMappingFieldDAO;

	@Autowired
	protected OAIFormatResolver formatResolver;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private MarcInterceptorFactory marcInterceptorFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	private String format;

	protected ImportConfiguration configuration;

	private RegexpExtractor idExtractor;

	private Transformer transformer;

	private final Map<Long, SourceMapping> mapping = new HashMap<>();

	@Override
	public List<HarvestedRecord> process(List<OAIRecord> oaiRecs) throws Exception {
		List<HarvestedRecord> results = new ArrayList<>();
		for (OAIRecord oaiRec : oaiRecs) {
			results.addAll(processHr(oaiRec));
		}
		return results;
	}

	protected List<HarvestedRecord> processHr(OAIRecord oaiRecord) throws TransformerException {
		List<HarvestedRecord> results = new ArrayList<>();
		if (!mapping.isEmpty()) {
			results.addAll(createMappedHarvestedRecord(oaiRecord));
		}
		results.add(createHarvestedRecord(oaiRecord, configuration));
		return results;
	}

	protected HarvestedRecord createHarvestedRecord(OAIRecord record, ImportConfiguration configuration) throws TransformerException {
		String recordId = idExtractor.extract(record.getHeader().getIdentifier());
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(
				recordId, configuration);
		boolean deleted = record.getHeader().isDeleted()
				|| record.getMetadata().getElement().getTagName().equals(METADATA_ERROR);
		byte[] recordContent = (deleted) ? null : asByteArray(record.getMetadata().getElement());
		if (recordContent != null && configuration.isInterceptionEnabled()) {
			MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(configuration, recordId, recordContent);
			if (interceptor != null) {
				//in case of invalid MARC is error processed later
				recordContent = interceptor.intercept();
			}
		}
		boolean recordContentEquals = false;
		if (rec == null) {
			// create new record
			HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(configuration, recordId);
			rec = new HarvestedRecord(id);
			rec.setHarvestedFrom(configuration);
			rec.setFormat(format);
			rec.setHarvested(new Date());
		} else if ((deleted && rec.getDeleted() != null && (rec.getRawRecord() == null || rec.getRawRecord().length == 0))
				|| ((recordContentEquals = Arrays.equals(recordContent, rec.getRawRecord())) && deleted == (rec.getDeleted() != null))) {
			rec.setLastHarvest(new Date());
			if (record.getHeader().getDatestamp() != null) {
				rec.setOaiTimestamp(record.getHeader().getDatestamp());
			}
			rec.setShouldBeProcessed(false);
			return rec; // no change in record
		}
		rec.setShouldBeProcessed(true);
		rec.setTemporalDeleted(rec.getDeleted() != null);
		rec.setTemporalUpdated(rec.getUpdated());
		rec.setTemporalContentEquals(recordContentEquals);
		rec.setUpdated(new Date());
		rec.setLastHarvest(new Date());
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
			Long confId = stepExecution.getJobParameters().getLong("configurationId");
			configuration = getImportConfiguration(confId);
			try {
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException tce) {
				throw new RuntimeException(tce);
			}
			try {
				List<ImportConfigurationMappingField> list = importConfigurationMappingFieldDAO.findByParentImportConf(confId);
				importConfigurationMappingFieldDAO.findByParentImportConf(confId).forEach(config -> {
					SourceMapping childConf = config.getSourceMapping();
					if (childConf != null) {
						mapping.put(childConf.getImportConfiguration().getId(), childConf);
					}
				});
			} catch (Exception e) {

			}
		}
	}

	protected ImportConfiguration getImportConfiguration(Long confId) {
		try {
			OAIHarvestConfiguration hc = configDao.get(confId);
			if (hc != null) {
				format = formatResolver.resolve(hc.getMetadataPrefix());
				String regex = MoreObjects.firstNonNull(hc.getRegex(), DEFAULT_EXTRACT_ID_PATTERN);
				idExtractor = new RegexpExtractor(regex);
				return hc;
			} else {
				DownloadImportConfiguration dic = downloadImportConfDao.get(confId);
				if (dic != null) {
					format = formatResolver.resolve(Constants.METADATA_FORMAT_XML_MARC);
					String regex = MoreObjects.firstNonNull(dic.getRegex(), DEFAULT_EXTRACT_ID_PATTERN);
					idExtractor = new RegexpExtractor(regex);
					return dic;
				}
			}
		} catch (Exception ex) {

		}
		return null;
	}

	protected List<HarvestedRecord> createMappedHarvestedRecord(OAIRecord oaiRecord) throws TransformerException {
		List<HarvestedRecord> results = new ArrayList<>();
		boolean deleted = oaiRecord.getHeader().isDeleted()
				|| oaiRecord.getMetadata().getElement().getTagName().equals(METADATA_ERROR);
		byte[] recordContent = (deleted) ? null : asByteArray(oaiRecord.getMetadata().getElement());
		if (deleted) {
			for (Entry<Long, SourceMapping> entry : mapping.entrySet()) {
				HarvestedRecord hr = createHarvestedRecord(oaiRecord, entry.getValue().getImportConfiguration());
				if (hr.getId() != null) results.add(hr);
			}
		} else {
			MarcRecord marcRecord = marcXmlParser.parseRecord(recordContent);
			for (Entry<Long, SourceMapping> entry : mapping.entrySet()) {
				SourceMapping mapping = entry.getValue();
				if ((mapping.getSubfield() == ' ' && mapping.getValue().equals(marcRecord.getControlField(mapping.getTag())))
						|| marcRecord.getFields(mapping.getTag(), mapping.getSubfield()).contains(mapping.getValue())) {
					results.add(createHarvestedRecord(oaiRecord, mapping.getImportConfiguration()));
				}
			}
		}
		return results;
	}

	protected byte[] asByteArray(Element element) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		transformer.transform(new DOMSource(element), result);
		return bos.toByteArray();
	}

}
