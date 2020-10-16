package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.miscellaneous.itemid.GenerateItemIdWriter;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Created by sergeyp on 7/13/17.
 */
public class ExportRecordsForClassifierProcessor implements ItemProcessor<HarvestedRecord.HarvestedRecordUniqueId, String> {

	private static Logger logger = LoggerFactory.getLogger(ExportRecordsForClassifierProcessor.class);

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

	private ProgressLogger progress = new ProgressLogger(logger, 1000);

	public ExportRecordsForClassifierProcessor(IOFormat format) {
		this.iOFormat = format;
	}

	@Override
	public String process(HarvestedRecord.HarvestedRecordUniqueId recordId) throws Exception {
//		if (record != null && record.getRawRecord().length != 0) {
//			MetadataRecord meta = metadataFactory.getMetadataRecord(record);
//			if (!meta.getLanguages().contains("cze") || !meta.getDetectedFormatList().contains(HarvestedRecordFormat.HarvestedRecordFormatEnum.BOOKS))
//				return null;
//			InputStream is = new ByteArrayInputStream(record.getRawRecord());
//			MarcRecord marcRecord = marcXmlParser.parseRecord(is);
//			if (marcRecord.getDataFields(OAI_FIELD).isEmpty()) {
//				marcRecord.addDataField(OAI_FIELD, ' ', ' ', "a", record.getUniqueId().getRecordId());
//			}
//			if (marcRecord.getDataFields("080").isEmpty() || !marcRecord.getDataFields("072").isEmpty()) return null;
//			else {
				HarvestedRecord kramRec = harvestedRecordDao.get(recordId);
		System.out.println(recordId);
//				for (HarvestedRecord harvestedRecord : harvestedRecordDao.getByDedupRecord(record.getDedupRecord())) {
//					if (harvestedRecord.getUniqueId().getHarvestedFromId() == 99001) kramRec = harvestedRecord;
//				}
				if (kramRec == null) return null;
				try {

					String fulltextPath = "/home/tomas/fit/fulltext/";
//					String tar = "/media/tomas/4edb0925-bd26-491f-b4f0-dc583fd566cb/home/tomas/fulltext/";
					String tar = "/home/tomas/fit/tar/";
//					new File(fulltextPath + kramRec.getUniqueId().getRecordId()).mkdir();
					StringBuilder sb = new StringBuilder();
					for (FulltextKramerius fk : kramDao.findAll(kramRec.getId())) {
						sb.append(new String(fk.getFulltext(), "UTF-8"));
					}
					Path file = Paths.get(fulltextPath + kramRec.getUniqueId().getRecordId() + ".txt");
					try {
						Files.write(file, Collections.singletonList(sb.toString()), Charset.forName("UTF-8"));
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}

//					for (FulltextKramerius fk : kramDao.findAll(kramRec.getId())) {
//						Path file = Paths.get(fulltextPath + kramRec.getUniqueId().getRecordId() + "/" + fk.getUuidPage() + ".txt");
//						try {
//							Files.write(file, Collections.singletonList(new String(fk.getFulltext(), "UTF-8")), Charset.forName("UTF-8"));
//						} catch (Exception e) {
//							System.out.println(e.getMessage());
//							continue;
//						}
//					}
					FileOutputStream fOut = null;
					BufferedOutputStream bOut = null;
					GzipCompressorOutputStream gzOut = null;
					TarArchiveOutputStream tOut = null;

					try {
//						String dirPath = fulltextPath + kramRec.getUniqueId().getRecordId() + "/";
						String dirPath = fulltextPath + kramRec.getUniqueId().getRecordId()+".txt";
						String tarGzPath = tar + kramRec.getUniqueId().getRecordId() + ".tar.gz";
						fOut = new FileOutputStream(new File(tarGzPath));
						bOut = new BufferedOutputStream(fOut);
						gzOut = new GzipCompressorOutputStream(bOut);
						tOut = new TarArchiveOutputStream(gzOut);
						tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
						addFileToTarGz(tOut, dirPath, "");
					} finally {
						tOut.finish();
						tOut.close();
						gzOut.close();
						bOut.close();
						fOut.close();
					}
//					FileUtils.deleteDirectory(new File(fulltextPath + kramRec.getUniqueId().getRecordId()));
					FileUtils.deleteQuietly(file.toFile());
				} catch (IOException ex) {
					// report
				}
				return "";
//			}
//		}
//		return null;
	}

	private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base)
			throws IOException {
		File f = new File(path);
		String entryName = base + f.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
		tOut.putArchiveEntry(tarEntry);

		if (f.isFile()) {
			FileInputStream in = new FileInputStream(f);
			IOUtils.copy(in, tOut);
			in.close();
			tOut.closeArchiveEntry();
		} else {
			tOut.closeArchiveEntry();
			File[] children = f.listFiles();
			if (children != null) {
				for (File child : children) {
					addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
				}
			}
		}
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
