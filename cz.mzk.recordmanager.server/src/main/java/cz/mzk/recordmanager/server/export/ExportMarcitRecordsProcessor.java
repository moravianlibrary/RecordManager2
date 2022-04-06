package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.LocDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class ExportMarcitRecordsProcessor implements ItemProcessor<HarvestedRecordUniqueId, String> {

	private final IOFormat iOFormat;

	private static final Logger logger = LoggerFactory.getLogger(ExportMarcitRecordsProcessor.class);

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private LocDAO locDAO;

	private final ProgressLogger progressLogger;

	public ExportMarcitRecordsProcessor(IOFormat format) {
		this.iOFormat = format;
		progressLogger = new ProgressLogger(logger, 10000);
	}

	@Override
	public String process(HarvestedRecordUniqueId recordId) throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(recordId);
		try {
			if (record != null && record.getRawRecord() != null && record.getRawRecord().length != 0) {
				InputStream is = new ByteArrayInputStream(record.getRawRecord());
				progressLogger.incrementAndLogProgress();
				switch (record.getFormat()) {
				case Constants.METADATA_FORMAT_MARC21:
					List<HarvestedRecord> locHrs = locDAO.findHrByLoc(record.getLocsAsString());
					if (locHrs.isEmpty()) {
						MarcRecord marcRecord = marcXmlParser.parseRecord(is);
						return marcRecord.export(iOFormat);
					}
					for (HarvestedRecord locHr : locHrs) {
						System.out.println(locHr);
						MarcRecord marcRecord = marcXmlParser.parseRecord(new ByteArrayInputStream(locHr.getRawRecord()));
						return marcRecord.export(iOFormat);
					}
				case Constants.METADATA_FORMAT_DUBLIN_CORE:
				case Constants.METADATA_FORMAT_ESE:
					return "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
