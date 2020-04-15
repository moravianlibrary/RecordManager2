package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.marc4j.marc.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ExportMarcFieldsProcessor implements ItemProcessor<Long, String> {

	private IOFormat iOFormat;

	private List<String> marcFields;

	private static Logger logger = LoggerFactory.getLogger(ExportMarcFieldsProcessor.class);

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private ProgressLogger progressLogger;

	public ExportMarcFieldsProcessor(IOFormat format, String marcFields) {
		this.iOFormat = format;
		this.marcFields = Arrays.asList(marcFields.split(","));
		progressLogger = new ProgressLogger(logger, 10000);
	}

	@Override
	public String process(Long id) {
		HarvestedRecord record = harvestedRecordDao.get(id);
		try {
			if (record == null || record.getDeleted() != null || record.getRawRecord() == null
					|| record.getRawRecord().length == 0) return null;
			progressLogger.incrementAndLogProgress();
			InputStream is = new ByteArrayInputStream(record.getRawRecord());
			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
			StringBuilder result = new StringBuilder();
			for (String tag : marcFields) {
				String field = marcRecord.getControlField(tag);
				if (field != null) {
					result.append(field);
					continue;
				}
				for (DataField dataField : marcRecord.getDataFields(tag)) {
					result.append(dataField.toString());
				}
			}
			return result.toString().isEmpty() ? null : result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
