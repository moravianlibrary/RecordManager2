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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	Writer writermarc = null;
	Writer writeraleph = null;
	Writer writerxml = null;

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
				HarvestedRecord kramRec = null;
				for (HarvestedRecord harvestedRecord : harvestedRecordDao.getByDedupRecord(record.getDedupRecord())) {
					if (harvestedRecord.getUniqueId().getHarvestedFromId() == 99001) kramRec = harvestedRecord;
				}
				if (kramRec == null) return null;
				try {
					new File("/home/tomas/fulltext/"+kramRec.getUniqueId().getRecordId()).mkdir();
					for (FulltextKramerius fk : kramDao.findAll(kramRec.getId())) {
						Path file = Paths.get("/home/tomas/fulltext/"+kramRec.getUniqueId().getRecordId()+"/"+fk.getUuidPage()+".txt");
						Files.write(file, Collections.singletonList(new String(fk.getFulltext(), "UTF-8")), Charset.forName("UTF-8"));
					}
					writerid.write(record.getUniqueId().getRecordId());
					writerid.write("\n");
					writermarc.write(marcRecord.export(IOFormat.LINE_MARC));
					writermarc.write("\n");
					writeraleph.write(marcRecord.export(IOFormat.ALEPH_MARC));
					writeraleph.write("\n");
					writerxml.write(marcRecord.export(IOFormat.XML_MARC));
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
			writermarc = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/tomas/metadata.mrc"), "utf-8"));
			writeraleph = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/tomas/metadata.aleph"), "utf-8"));
			writerxml = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/tomas/metadata.xml"), "utf-8"));
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
			writerxml.close();
			writeraleph.close();
			writermarc.close();
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
