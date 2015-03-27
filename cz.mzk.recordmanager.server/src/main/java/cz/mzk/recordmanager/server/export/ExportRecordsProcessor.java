package cz.mzk.recordmanager.server.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class ExportRecordsProcessor implements ItemProcessor<Long, String> {

	private IOFormat iOFormat;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	public ExportRecordsProcessor(IOFormat format) {
		this.iOFormat = format;
	}
	
	@Override
	public String process(Long recordId) throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(recordId);
		if (record != null) {
			InputStream is = new ByteArrayInputStream(record.getRawRecord());
			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
			return marcRecord.export(iOFormat);
		} 
		return null;
	}

}
