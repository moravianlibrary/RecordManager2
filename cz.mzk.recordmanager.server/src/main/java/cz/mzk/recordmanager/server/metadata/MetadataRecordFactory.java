package cz.mzk.recordmanager.server.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.institutions.AuthMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.KramDefaultMetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.institutions.MzkMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.MzkNormsMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.NkpMarcMetadataRecord;
import cz.mzk.recordmanager.server.metadata.institutions.SfxMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.SfxjibNlkPeriodicalsMetadataMarcRecord;
import cz.mzk.recordmanager.server.metadata.institutions.SkatMarcMetadataRecord;
import cz.mzk.recordmanager.server.metadata.institutions.TreMetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
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

		ImportConfiguration configuration = record.getHarvestedFrom();
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		
		String prefix = "";
		if (configuration != null) {
			prefix = configuration.getIdPrefix();
			prefix = prefix == null ? "" : prefix;
		}
		
		String recordFormat = record.getFormat();
		
        if (Constants.METADATA_FORMAT_MARC21.equals(recordFormat) 
        		|| Constants.METADATA_FORMAT_XML_MARC.equals(recordFormat)
        		|| Constants.METADATA_FORMAT_MARC_CPK.equals(recordFormat)
        		|| Constants.METADATA_FORMAT_OAI_MARCXML_CPK.equals(recordFormat)
        		|| Constants.METADATA_FORMAT_MARC21E.equals(recordFormat)) {
    		MarcRecord marcRec = marcXmlParser.parseRecord(is);
			switch (prefix) {
			case Constants.PREFIX_MZK:
				return new MzkMetadataMarcRecord(marcRec);
			case Constants.PREFIX_NKP:
				return new NkpMarcMetadataRecord(marcRec);
			case Constants.PREFIX_TRE:
			case Constants.PREFIX_MKUO:
				return new TreMetadataMarcRecord(marcRec);
			case Constants.PREFIX_MZKNORMS:
				return new MzkNormsMetadataMarcRecord(marcRec);
			case Constants.PREFIX_SFXJIBMZK:
			case Constants.PREFIX_SFXJIBNLK:
			case Constants.PREFIX_SFXKNAV:
				return new SfxMetadataMarcRecord(marcRec);
			case Constants.PREFIX_SFXJIBNLK_PERIODICALS:
				return new SfxjibNlkPeriodicalsMetadataMarcRecord(marcRec);
			case Constants.PREFIX_CASLIN:
				return new SkatMarcMetadataRecord(marcRec);
			case Constants.PREFIX_AUTH:
				return new AuthMetadataMarcRecord(marcRec);
			default:
				return new MetadataMarcRecord(marcRec);
			}
		}
        
        if (Constants.METADATA_FORMAT_DUBLIN_CORE.equals(recordFormat)
        		|| Constants.METADATA_FORMAT_ESE.equals(recordFormat)) {
        	DublinCoreRecord dcRec = dcParser.parseRecord(is);
        	switch(prefix){
			case Constants.PREFIX_KRAM_MZK:
			case Constants.PREFIX_KRAM_NTK:
			case Constants.PREFIX_KRAM_KNAV:
			case Constants.PREFIX_KRAM_NKP:
				return new KramDefaultMetadataDublinCoreRecord(dcRec);
			default:
				return getMetadataRecord(dcRec);
        	}
			
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
