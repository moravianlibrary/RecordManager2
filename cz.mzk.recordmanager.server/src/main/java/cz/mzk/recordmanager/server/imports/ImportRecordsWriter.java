package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

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
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import cz.mzk.recordmanager.server.util.RegexpExtractor;

public class ImportRecordsWriter implements ItemWriter<List<Record>>, StepExecutionListener {
	
	private static Logger logger = LoggerFactory.getLogger(ImportRecordsWriter.class);
	
	@Autowired
	private DelegatingDedupKeysParser dedupKeysParser;
	
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

	public ImportRecordsWriter(Long configurationId) {
		this.configurationId = configurationId;
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
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.setConverter(ISOCharConvertor.INSTANCE);
					marcWriter.write(currentRecord);
					marcWriter.close();
					byte[] recordContent = outStream.toByteArray();
					if (harvestConfiguration.isInterceptionEnabled()) {
						MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(harvestConfiguration, recordContent);
						if (interceptor != null) {
							recordContent = interceptor.intercept();
						}
					}
					InputStream is = new ByteArrayInputStream(recordContent);
					MarcRecord marc = marcXmlParser.parseRecord(is);
					MetadataRecord metadata = metadataFactory.getMetadataRecord(marc, harvestConfiguration);
					String recordId = metadata.getOAIRecordId();
					if (recordId == null) {
						recordId = metadata.getUniqueId();
					}
					if (regexpExtractor != null) {
						recordId = regexpExtractor.extract(recordId);
					}
					HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(recordId, configurationId);
					if (hr == null){
						HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(harvestConfiguration, recordId);
						hr = new HarvestedRecord(id);
						// TODO detect format
						hr.setFormat("marc21-xml");
						hr.setHarvestedFrom(harvestConfiguration);
					}
					hr.setUpdated(new Date());
					
					if(metadata.isDeleted()) {
						hr.setDeleted(new Date());
						hr.setRawRecord(new byte[0]);
					}
					else {
						hr.setDeleted(null);
						hr.setRawRecord(recordContent);
					}
					if (harvestConfiguration.isGenerateDedupKeys()) {
						harvestedRecordDao.persist(hr);
						dedupKeysParser.parse(hr, metadata);	
					}
					
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
