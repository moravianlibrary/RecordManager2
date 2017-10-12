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

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.TezaurusRecord;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.TezaurusDAO;

@Component
@StepScope
public class ImportTezaurusRecordsWriter implements ItemWriter<List<Record>> {

	private static Logger logger = LoggerFactory
			.getLogger(ImportTezaurusRecordsWriter.class);

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfDao;

	@Autowired
	private TezaurusDAO tezaurusDao;

	@Autowired
	private MetadataRecordFactory metadataFactory;

	private ImportConfiguration config;

	private Long confId;

	public ImportTezaurusRecordsWriter(Long configraionId) {
		this.confId = configraionId;
	}

	@Override
	public void write(List<? extends List<Record>> items) throws Exception {
		if (config == null) {
			config = oaiHarvestConfDao.get(confId);
		}
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				try {
					MarcRecord marc = new MarcRecordImpl(currentRecord);
					MetadataRecord metadata = metadataFactory
							.getMetadataRecord(marc, config);
					String recordId = metadata.getOAIRecordId();
					if (recordId == null) recordId = metadata.getUniqueId();
					TezaurusRecord tr = tezaurusDao
							.findByIdAndHarvestConfiguration(recordId, config);
					if (tr == null) {
						tr = new TezaurusRecord();
						tr.setRecordId(recordId);
						tr.setHarvestedFrom(config);
					}
					tr.setTezaurusKey(metadata.getTezaurusKey());
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.write(currentRecord);
					marcWriter.close();
					byte[] recordContent = outStream.toByteArray();
					tr.setRawRecord(recordContent);
					tezaurusDao.persist(tr);
				} catch (Exception e) {
					logger.warn("Error occured in processing record");
					throw e;
				}
			}
		}
	}
}
