package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

/**
 * Created by sergeyp on 7/13/17.
 */
public class ExportRecordsForClassifierProcessor implements ItemProcessor<HarvestedRecord.HarvestedRecordUniqueId, String>, StepExecutionListener {

	private IOFormat iOFormat;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Autowired
	private FulltextKrameriusDAO kramDao;

	private static final String OAI_FIELD = "OAI";

	public ExportRecordsForClassifierProcessor(IOFormat format) {
		this.iOFormat = format;
	}

	Writer writer = null;
	Writer writerid = null;

	@Override
	public String process(HarvestedRecord.HarvestedRecordUniqueId recordId) throws Exception {
		HarvestedRecord record = harvestedRecordDao.get(recordId);
		if (record != null && record.getRawRecord().length != 0) {
			MetadataRecord meta = metadataFactory.getMetadataRecord(record);
			if (!meta.getLanguages().contains("cze") || !meta.getDetectedFormatList().contains(HarvestedRecordFormat.HarvestedRecordFormatEnum.BOOKS))
				return null;
			InputStream is = new ByteArrayInputStream(record.getRawRecord());
			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
			if (marcRecord.getDataFields(OAI_FIELD).isEmpty()) {
				marcRecord.addDataField(OAI_FIELD, ' ', ' ', "a", record.getUniqueId().getRecordId());
			}
			if (marcRecord.getDataFields("080").isEmpty() || !marcRecord.getDataFields("072").isEmpty()) return null;
			else {
				try {
					StringBuilder full = new StringBuilder();
					for (String fk : kramDao.getFullText(record.getDedupRecord())) {
						full.append(fk.replaceAll("(\\r|\\n)", ""));

					}
					writer.write(full.toString());
					writer.write("\n");
					writerid.write(record.getUniqueId().getRecordId());
					writerid.write("\n");
				} catch (IOException ex) {
					// report
				}
				return marcRecord.export(iOFormat);
			}
		}
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/tomas/fulltext.txt"), "utf-8"));
			writerid = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/tomas/classid.txt"), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			writer.close();
			writerid.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

//	@Override
//	public String process(HarvestedRecord.HarvestedRecordUniqueId recordId) throws Exception {
//		HarvestedRecord record = harvestedRecordDao.get(recordId);
//		if (record != null && record.getRawRecord().length != 0) {
//			MetadataRecord meta = metadataFactory.getMetadataRecord(record);
//			if (!meta.getLanguages().contains("cze") || !meta.getDetectedFormatList().contains(HarvestedRecordFormat.HarvestedRecordFormatEnum.BOOKS))
//				return null;
//			InputStream is = new ByteArrayInputStream(record.getRawRecord());
//			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
//			if (marcRecord.getDataFields(OAI_FIELD).isEmpty()) {
//				marcRecord.addDataField(OAI_FIELD, ' ', ' ', "a", record.getUniqueId().getRecordId());
//			}
//
//			if (marcRecord.getDataFields("080").isEmpty()) return null;
//			else {
//
//				Writer writer = null;
//
//				try {
//					writer = new BufferedWriter(new OutputStreamWriter(
//							new FileOutputStream("/home/tomas/fulltext/"+record.getUniqueId().getRecordId()+".txt"), "utf-8"));
//					for (String fk : kramDao.getFullText(record.getDedupRecord())) {
//						writer.write(fk);
//					}
//					writer.write("Something");
//				} catch (IOException ex) {
//					// report
//				} finally {
//					try {writer.close();} catch (Exception ex) {/*ignore*/}
//				}
//				marcRecord.export(iOFormat);
//			}
//		}
//		return null;
//	}
}
