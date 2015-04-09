package cz.mzk.recordmanager.server.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public class MetadataRecordFactory {
	
	@Autowired 
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	public MetadataRecord getMetadataRecord(HarvestedRecord record) {
		if (record == null) {
			return null;
		}
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		MarcRecord rec = marcXmlParser.parseRecord(is);
		return getMetadataRecord(rec);
	}
	
	public MetadataRecord getMetadataRecord(HarvestedRecordId recordId) {
		return getMetadataRecord(harvestedRecordDao.get(recordId));
	}
	
	public MetadataRecord getMetadataRecord(MarcRecord marcRecord) {
		MetadataRecord metadata = new MetadataMarcRecord(marcRecord);
		return metadata;
	}
}
