package cz.mzk.recordmanager.server.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.institutions.MzkMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.NkpMarcMetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

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

		OAIHarvestConfiguration configuration = record.getHarvestedFrom();
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		MarcRecord rec = marcXmlParser.parseRecord(is);
		
		if (configuration == null) {
			return getMetadataRecord(rec);
		}
		
		switch (configuration.getIdPrefix()) {
		case Constants.PREFIX_MKZ:
			return new MzkMetadataMarcRecord(rec);
		case Constants.PREFIX_NKP:
			return new NkpMarcMetadataRecord(rec);
		default:
			return new MetadataMarcRecord(rec);
		}
	}
	
	public MetadataRecord getMetadataRecord(HarvestedRecordUniqueId recordId) {
		return getMetadataRecord(harvestedRecordDao.get(recordId));
	}
	
	public MetadataRecord getMetadataRecord(MarcRecord marcRecord) {
		MetadataRecord metadata = new MetadataMarcRecord(marcRecord);
		return metadata;
	}
}
