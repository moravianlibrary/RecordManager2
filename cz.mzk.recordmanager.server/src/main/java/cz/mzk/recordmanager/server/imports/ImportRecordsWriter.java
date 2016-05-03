package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.CharMatcher;

import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.marc.ISOCharConvertor;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

@Component
@StepScope
public class ImportRecordsWriter implements ItemWriter<List<Record>> {
	
	private static Logger logger = LoggerFactory.getLogger(ImportRecordsWriter.class);
	
	@Autowired
	private DelegatingDedupKeysParser dedupKeysParser;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private MetadataRecordFactory metadataFactory;
	
	@Autowired 
	private MarcInterceptorFactory marcInterceptorFactory;

	@Autowired
	protected SessionFactory sessionFactory;

	private OAIHarvestConfiguration harvestConfiguration;

	private Long configurationId;

	public ImportRecordsWriter(Long configraionId) {
		this.configurationId = configraionId;
	}

	public void write(List<? extends List<Record>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	protected void writeInner(List<? extends List<Record>> items) throws Exception {
		if (harvestConfiguration == null) {
			harvestConfiguration = oaiHarvestConfigurationDao.get(configurationId);			
		}
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				try {
					MarcRecord marc = new MarcRecordImpl(currentRecord);
					MetadataRecord metadata = metadataFactory.getMetadataRecord(marc);
					String recordId = metadata.getOAIRecordId();
					if(recordId == null) recordId = metadata.getUniqueId();
					HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(recordId, configurationId);
					if(hr == null){
						HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(harvestConfiguration, recordId);
						hr = new HarvestedRecord(id);
//						TODO detect format
						hr.setFormat("marc21-xml");
						hr.setHarvestedFrom(harvestConfiguration);
					}
					hr.setUpdated(new Date());
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.setConverter(ISOCharConvertor.INSTANCE);
					marcWriter.write(currentRecord);
					marcWriter.close();
					byte[] recordContent = outStream.toByteArray();
					if (harvestConfiguration.isInterceptionEnabled()) {
						MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(harvestConfiguration,recordContent);
						if (interceptor != null) {
							recordContent = interceptor.intercept();
						}
					}
					if(metadata.isDeleted()) {
						hr.setDeleted(new Date());
						hr.setRawRecord(new byte[0]);
					}
					else {
						hr.setDeleted(null);
						hr.setRawRecord(recordContent);
					}
					harvestedRecordDao.persist(hr);
					dedupKeysParser.parse(hr, metadata);
					
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
}
