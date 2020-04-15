package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class ExportMarcFieldsProcessor implements ItemProcessor<HarvestedRecordUniqueId, String> {

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
	public String process(HarvestedRecordUniqueId recordId) throws Exception {

		return null;
	}

}
