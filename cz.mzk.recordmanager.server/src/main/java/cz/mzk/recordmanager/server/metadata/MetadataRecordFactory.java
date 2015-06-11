package cz.mzk.recordmanager.server.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.institutions.MzkMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.NkpMarcMetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class MetadataRecordFactory {
	
	@Autowired 
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Autowired
	private DublinCoreParser dcParser;
	
	public MetadataRecord getMetadataRecord(HarvestedRecord record) {
		if (record == null) {
			return null;
		}

		OAIHarvestConfiguration configuration = record.getHarvestedFrom();
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		
		String prefix = "";
		if (configuration != null) {
			prefix = configuration.getIdPrefix();
			prefix = prefix == null ? "" : prefix;
		}
		
		String recordFormat = record.getFormat();
		
        if (Constants.METADATA_FORMAT_MARC21.equals(recordFormat) 
        		|| Constants.METADATA_FORMAT_XML_MARC.equals(recordFormat)) {
        	
    		MarcRecord marcRec = marcXmlParser.parseRecord(is);
			switch (prefix) {
			case Constants.PREFIX_MKZ:
				return new MzkMetadataMarcRecord(marcRec);
			case Constants.PREFIX_NKP:
				return new NkpMarcMetadataRecord(marcRec);
			default:
				return new MetadataMarcRecord(marcRec);
			}
		}
        
        if (Constants.METADATA_FORMAT_DUBLIN_CORE.equals(recordFormat)) {
        	DublinCoreRecord dcRec = dcParser.parseRecord(is);
			return getMetadataRecord(dcRec);
        }
        
        return null;
    }
	
	public MetadataRecord getMetadataRecord(HarvestedRecordUniqueId recordId) {
		return getMetadataRecord(harvestedRecordDao.get(recordId));
	}
	
	public MetadataRecord getMetadataRecord(MarcRecord marcRecord) {
		MetadataRecord metadata = new MetadataMarcRecord(marcRecord);
		return metadata;
	}

	public MetadataRecord getMetadataRecord(DublinCoreRecord dcRecord) {
		MetadataRecord metadata = new MetadataDublinCoreRecord(dcRecord);
		return metadata;
	}
}
