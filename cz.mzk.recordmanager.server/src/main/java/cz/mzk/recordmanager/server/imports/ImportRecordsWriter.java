package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cz.mzk.recordmanager.server.bibliolinker.keys.DelegatingBiblioLinkerKeysParser;
import org.hibernate.SessionFactory;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.marc.ISOCharConvertor;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import cz.mzk.recordmanager.server.util.RegexpExtractor;

public class ImportRecordsWriter implements ItemWriter<List<Record>>, StepExecutionListener {
	
	private static Logger logger = LoggerFactory.getLogger(ImportRecordsWriter.class);
	
	@Autowired
	private DelegatingDedupKeysParser dedupKeysParser;

	@Autowired
	protected DelegatingBiblioLinkerKeysParser biblioLinkerKeysParser;

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao;
	
	@Autowired
	private DownloadImportConfigurationDAO downloadImportConfigurationDao;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private MetadataRecordFactory metadataFactory;
	
	@Autowired 
	private MarcInterceptorFactory marcInterceptorFactory;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	private ImportConfiguration harvestConfiguration;

	private Long configurationId;

	private RegexpExtractor regexpExtractor;

	private ProgressLogger progress;
	
	public ImportRecordsWriter(Long configurationId) {
		this.configurationId = configurationId;
		this.progress = new ProgressLogger(logger, 5000);
	}

	@Override
	public void write(List<? extends List<Record>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	protected void writeInner(List<? extends List<Record>> items) throws Exception {
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				try {
					progress.incrementAndLogProgress();
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.setConverter(ISOCharConvertor.INSTANCE);
					marcWriter.write(currentRecord);
					marcWriter.close();
					// need recordId before interception
					byte[] recordContent = outStream.toByteArray();
					MetadataRecord metadata = parseMetadata(recordContent);
					String recordId = metadata.getUniqueId();
					if (regexpExtractor != null) {
						recordId = regexpExtractor.extract(recordId);
					}
					if (harvestConfiguration.isInterceptionEnabled()) {
						MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(harvestConfiguration, recordId, recordContent);
						if (interceptor != null) {
							byte[] recordContentNew = interceptor.intercept();
							if (!Arrays.equals(recordContent, recordContentNew)) {
								// if record content was changed, parse metadata again
								metadata = parseMetadata(recordContentNew);
								// set intercepted content
								recordContent = recordContentNew;
							}
						}
					}
					if (recordId == null) { // record without record_id
						logger.info("Record without ID");
						logger.debug(new String(recordContent));
						continue;
					}
					HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(recordId, configurationId);
					if (hr == null){
						HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(harvestConfiguration, recordId);
						hr = new HarvestedRecord(id);
						// TODO detect format
						hr.setFormat("marc21-xml");
						hr.setHarvestedFrom(harvestConfiguration);
					}
					else if (Arrays.equals(recordContent, hr.getRawRecord())) {
						hr.setLastHarvest(new Date());
						harvestedRecordDao.persist(hr);
						continue;
					}
					hr.setUpdated(new Date());
					hr.setLastHarvest(new Date());
					hr.setDeleted(null);
					hr.setRawRecord(recordContent);

					harvestedRecordDao.persist(hr);
					dedupKeysParser.parse(hr, metadata);
					biblioLinkerKeysParser.parse(hr, metadata);

					if (harvestConfiguration.isFilteringEnabled() && !hr.getShouldBeProcessed()) {
						logger.debug("Filtered record: " + hr.getUniqueId());
						hr.setDeleted(new Date());
					}

					harvestedRecordDao.persist(hr);
				} catch (Exception e) {
					logger.warn("Error occured in processing record");
					throw e;
				}
			}
		}
	}

	private MetadataRecord parseMetadata(byte[] recordContent) {
		InputStream is = new ByteArrayInputStream(recordContent);
		MarcRecord marc = marcXmlParser.parseRecord(is);
		return metadataFactory.getMetadataRecord(marc, harvestConfiguration);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder session = sync.register()) {
			OAIHarvestConfiguration hc = oaiHarvestConfigurationDao.get(configurationId);
			String regex = null;
			if(hc != null){
				regex = hc.getRegex();
				harvestConfiguration = hc;
			}
			else{
				DownloadImportConfiguration dic = downloadImportConfigurationDao.get(configurationId);
				if(dic != null){
					regex = dic.getRegex();
					harvestConfiguration = dic;
				}
			}
			
			if (regex != null) {
				regexpExtractor = new RegexpExtractor(regex);
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
