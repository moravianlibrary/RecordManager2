package cz.mzk.recordmanager.server.imports;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

public class ImportRecordsProcessor implements ItemProcessor<List<Record>, List<HarvestedRecord>> {

	@Autowired
	private DelegatingDedupKeysParser dedupKeysParser;
	
	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao;
	
	private OAIHarvestConfiguration harvestConfiguration;
	
	private String format;
	
	private Calendar calendar = Calendar.getInstance();
	
	public ImportRecordsProcessor(Long configurationId) {
		harvestConfiguration = oaiHarvestConfigurationDao.get(configurationId);
	}
	
	@Override
	public List<HarvestedRecord> process(List<Record> inRecords) throws Exception {
		List<HarvestedRecord> result = new ArrayList<HarvestedRecord>();
		for (Record currentRecord : inRecords) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
			marcWriter.write(currentRecord);
			HarvestedRecordId id = new HarvestedRecordId(harvestConfiguration, null);
			HarvestedRecord hr = new HarvestedRecord(id);
			hr.setRawRecord(outStream.toByteArray());
		}
		return null;
	}

}
