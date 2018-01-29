package cz.mzk.recordmanager.server.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.Cosmotron996;
import cz.mzk.recordmanager.server.util.ProgressLogger;

public class ExportCosmotron996Processor implements ItemProcessor<Cosmotron996, String> {

	private IOFormat iOFormat;

	private static Logger logger = LoggerFactory.getLogger(ExportCosmotron996Processor.class);

	@Autowired
	private MarcXmlParser marcXmlParser;

	private static final String OAI_FIELD = "OAI";

	private ProgressLogger progressLogger;

	public ExportCosmotron996Processor(IOFormat format) {
		this.iOFormat = format;
		progressLogger = new ProgressLogger(logger, 10000);
	}

	@Override
	public String process(Cosmotron996 cosmo996) throws Exception {
		if (cosmo996 != null && cosmo996.getRawRecord() != null && cosmo996.getRawRecord().length != 0) {
			InputStream is = new ByteArrayInputStream(cosmo996.getRawRecord());
			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
			if (marcRecord.getDataFields(OAI_FIELD).isEmpty()) {
				marcRecord.addDataField(OAI_FIELD, ' ', ' ', "a", cosmo996.getRecordId());
			}
			progressLogger.incrementAndLogProgress();
			return marcRecord.export(iOFormat);
		}
		return null;
	}

}
