package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.Cosmotron996DAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.util.CosmotronUtils;

@Component
@StepScope
public class ImportCosmotron996RecordsWriter implements ItemWriter<List<Record>> {
	
	private static Logger logger = LoggerFactory.getLogger(ImportCosmotron996RecordsWriter.class);
	
	@Autowired
	private DelegatingDedupKeysParser dedupKeysParser;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private Cosmotron996DAO cosmotron996Dao;
	
	@Autowired
	private MetadataRecordFactory metadataFactory;
	
	@Autowired 
	private MarcInterceptorFactory marcInterceptorFactory;
	
	private OAIHarvestConfiguration harvestConfiguration;
	
	private Long configurationId;
	
	
	public ImportCosmotron996RecordsWriter(Long configraionId) {
		this.configurationId = configraionId;
	}

	@Override
	public void write(List<? extends List<Record>> items) throws Exception {
		if (harvestConfiguration == null) {
			harvestConfiguration = oaiHarvestConfigurationDao.get(configurationId);			
		}
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				try {
					MarcRecord marc = new MarcRecordImpl(currentRecord);
					String parentId = CosmotronUtils.get77308w(marc);
					MetadataRecord metadata = metadataFactory.getMetadataRecord(marc, harvestConfiguration);
					String recordId = metadata.getOAIRecordId();
					HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(parentId, configurationId);
					if(hr == null) continue;
					Cosmotron996 c996 = cosmotron996Dao.findByIdAndHarvestConfiguration(recordId, configurationId);
					if(c996 == null){
						c996 = new Cosmotron996();
						c996.setRecordId(recordId);
						c996.setHarvestedFrom(configurationId);
						c996.setHarvestedRecord(hr);
					}
					c996.setUpdated(new Date());
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.write(currentRecord);
					marcWriter.close();
					byte[] recordContent = outStream.toByteArray();
					if (harvestConfiguration.isInterceptionEnabled()) {
						MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(harvestConfiguration,recordContent);
						if (interceptor != null) {
							recordContent = interceptor.intercept();
						}
					}
					c996.setDeleted(null);
					c996.setRawRecord(recordContent);
					harvestedRecordDao.persist(CosmotronUtils.update996(hr, c996));
				} catch (Exception e) {
					logger.warn("Error occured in processing record");
					throw e;
				}
			}
		}
	}
}
