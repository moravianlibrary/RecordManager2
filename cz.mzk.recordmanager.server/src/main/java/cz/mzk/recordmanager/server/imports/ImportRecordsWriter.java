package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayOutputStream;
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
import cz.mzk.recordmanager.server.model.HarvestedRecord;
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
	
	private OAIHarvestConfiguration harvestConfiguration;
	
	private Long configurationId;
	
	
	public ImportRecordsWriter(Long configraionId) {
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
					HarvestedRecord hr = new HarvestedRecord();
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.write(currentRecord);
					marcWriter.close();
					
					MarcRecord marc = new MarcRecordImpl(currentRecord);
					hr.setRecordId(marc.getUniqueId());
					hr.setRawRecord(outStream.toByteArray());
					hr.setHarvestedFrom(harvestConfiguration);
//					TODO detect format
					hr.setFormat("marc21-xml");
					
					dedupKeysParser.parse(hr);
					harvestedRecordDao.persist(hr);
				} catch (Exception e) {
					logger.warn("Error occured in processing record");
					throw e;
				}
			}
		}
	}
}
