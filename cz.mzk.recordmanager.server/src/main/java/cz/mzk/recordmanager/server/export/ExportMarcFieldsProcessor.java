package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
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

	private final List<String> marcFields;

	private static final Logger logger = LoggerFactory.getLogger(ExportMarcFieldsProcessor.class);

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private final ProgressLogger progressLogger;

	private static final String EXPORT_FORMAT = "%s\n";

	public ExportMarcFieldsProcessor(String marcFields) {
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
					result.append(tag).append(" ").append(field);
					result.append("\n");
					continue;
				}
				for (DataField dataField : marcRecord.getDataFields(tag)) {
					result.append(getExportText(record.getHarvestedFrom().getIdPrefix(),
							record.getUniqueId().getRecordId(), dataField.toString()));
				}
			}
			switch (record.getFormat()) {
				case Constants.METADATA_FORMAT_MARC21:
					result.append("OAI   $a").append(record.getHarvestedFrom().getIdPrefix()).append(".").append(record.getUniqueId().getRecordId());
					result.append("\n");
			}
			return result.toString().isEmpty() ? null : result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getExportText(String prefix, String recordId, String field) {
		return String.format(EXPORT_FORMAT, field);
	}

}
