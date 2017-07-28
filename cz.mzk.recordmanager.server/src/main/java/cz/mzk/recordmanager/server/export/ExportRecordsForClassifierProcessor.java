package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by sergeyp on 7/13/17.
 */
public class ExportRecordsForClassifierProcessor implements ItemProcessor<HarvestedRecord.HarvestedRecordUniqueId, String> {

	private IOFormat iOFormat;

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private static final String OAI_FIELD = "OAI";

	public ExportRecordsForClassifierProcessor(IOFormat format) {
		this.iOFormat = format;
	}

	@Override
	public String process(HarvestedRecord.HarvestedRecordUniqueId recordId) throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(recordId);
		if (record != null && record.getRawRecord().length != 0) {
			InputStream is = new ByteArrayInputStream(record.getRawRecord());
			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
			if(marcRecord.getDataFields(OAI_FIELD).isEmpty()){
				marcRecord.addDataField(OAI_FIELD, ' ', ' ', new String[]{"a", record.getUniqueId().getRecordId()});
			}

			return marcRecord.getDataFields("080").isEmpty()  ? null: marcRecord.export(iOFormat);
		}
		return null;
	}
}
