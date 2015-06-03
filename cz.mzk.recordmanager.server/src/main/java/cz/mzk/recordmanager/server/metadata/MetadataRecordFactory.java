package cz.mzk.recordmanager.server.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

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
		InputStream is = new ByteArrayInputStream(record.getRawRecord());

		if (record.getFormat().equals("marc21-xml")) {
			MarcRecord rec = marcXmlParser.parseRecord(is);
			return getMetadataRecord(rec);
		} else if (record.getFormat().equals("dublinCore")) {
			DublinCoreRecord rec = dcParser.parseRecord(is);
			return getMetadataRecord(rec);
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
