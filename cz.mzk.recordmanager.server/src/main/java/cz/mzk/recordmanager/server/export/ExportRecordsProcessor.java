package cz.mzk.recordmanager.server.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;

public class ExportRecordsProcessor implements ItemProcessor<HarvestedRecordUniqueId, String> {

	private IOFormat iOFormat;

	private static Logger logger = LoggerFactory.getLogger(ExportRecordsProcessor.class);

	@Autowired
	private MetadataRecordFactory metadataFactory;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	private static final String OAI_FIELD = "OAI";
	
	private ProgressLogger progressLogger;

	public ExportRecordsProcessor(IOFormat format) {
		this.iOFormat = format;
		progressLogger = new ProgressLogger(logger, 10000);
	}
	
	@Override
	public String process(HarvestedRecordUniqueId recordId) throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(recordId);
		if (record != null && record.getRawRecord().length != 0) {
			InputStream is = new ByteArrayInputStream(record.getRawRecord());
			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
			if(marcRecord.getDataFields(OAI_FIELD).isEmpty()){
				marcRecord.addDataField(OAI_FIELD, ' ', ' ', new String[]{"a", record.getUniqueId().getRecordId()});
			}
			progressLogger.incrementAndLogProgress();
			return marcRecord.export(iOFormat);
		} 
		return null;
	}

}
