package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.marc4j.*;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImportRecordsFileReader implements ItemReader<List<Record>> {

	private static Logger logger = LoggerFactory.getLogger(ImportRecordsFileReader.class);

	@Autowired
	private ImportConfigurationDAO configDao;

	@Autowired
	private DownloadImportConfigurationDAO dicDao;

	@Autowired
	private HttpClient httpClient;

	private MarcReader reader;

	private IOFormat format;

	private Long confId;

	private int batchSize = 20;

	private List<String> files = null;

	public ImportRecordsFileReader(Long confId, String filename, String strFormat) throws FileNotFoundException {
		format = IOFormat.stringToExportFormat(strFormat);
		this.confId = confId;
		getFilesName(filename);
		filterFileNames();
	}

	public ImportRecordsFileReader(Long confId) throws Exception {
		this.confId = confId;
		this.reader = null;
	}

	@Override
	public synchronized List<Record> read() throws Exception {
		List<Record> batch = new ArrayList<>();

		if (reader == null)
			if (files == null) initializeDownloadReader();
			else initializeFilesReader();
		else if (!reader.hasNext()) initializeFilesReader();
		try {
			while (reader.hasNext()) {
				try {
					batch.add(reader.next());
				} catch (MarcException e) {
					logger.warn(e.getMessage());
				}
				if (batch.size() >= batchSize) {
					break;
				}
			}
		} catch (MarcException e) {
			return null;
		}
		return batch.isEmpty() ? null : batch;
	}

	private MarcReader getMarcReader(InputStream inStream) {
		switch (format) {
		case LINE_MARC:
			return new MarcLineStreamReader(inStream);
		case ALEPH_MARC:
			return new MarcAlephStreamReader(inStream);
		case ISO_2709:
			return new MarcISO2709StreamReader(inStream, "UTF-8");
		case XML_PATENTS:
			return new PatentsXmlStreamReader(inStream);
		case XML_PATENTS_ST36:
			return new PatentsSt36XmlStreamReader(inStream);
		case OSOBNOSTI_REGIONU:
			return new OsobnostiRegionuXmlStreamReader(inStream);
		case SFX:
			return new SfxJibXmlStreamReader(inStream, configDao.get(confId).getIdPrefix());
		case SFX_NLK:
			return new SfxJibNlkCsvStreamReader(inStream, configDao.get(confId).getIdPrefix());
		case MUNIPRESS:
			return new MunipressCsvStreamReader(inStream);
		case PALMKNIHY:
			return new PalmKnihyXmlStreamReader(inStream);
		default:
			return new MarcXmlReader(inStream);
		}
	}

	private void initializeDownloadReader() throws IOException {
		DownloadImportConfiguration config = dicDao.get(confId);
		if (config == null) {
			throw new IllegalArgumentException(String.format("Configuration with id=%s not found.", confId));
		}
		String url = config.getUrl();
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("Missing url in DownloadImportConfiguration with id=%s.", confId));
		}
		this.format = IOFormat.stringToExportFormat(config.getFormat());
		this.reader = getMarcReader(httpClient.executeGet(url));
	}

	private void initializeFilesReader() {
		try {
			if (files != null && !files.isEmpty()) {
				String file = files.remove(0);
				FileInputStream inStream = new FileInputStream(file);
				reader = getMarcReader(inStream);
			}
		} catch (FileNotFoundException e) {
			logger.warn(e.getMessage());
		}
	}

	private void getFilesName(String filename) throws FileNotFoundException {
		if (filename.isEmpty()) return;
		if (files == null) files = new ArrayList<>();
		File f = new File(filename);
		if (f.isFile()) {
			files.add(f.getAbsolutePath());
		} else {
			File[] listFiles = f.listFiles();
			if (listFiles == null) throw new FileNotFoundException();
			for (File file : listFiles) {
				if (file.isDirectory()) getFilesName(file.getAbsolutePath());
				else files.add(file.getAbsolutePath());
			}
		}
	}

	private void filterFileNames() {
		switch (format) {
			case XML_PATENTS:
				files.removeIf(file -> !file.endsWith(".xml"));
				files.removeIf(file -> file.endsWith("content.xml"));
				files.removeIf(file -> file.contains("/st26"));
				break;
		}
	}

}
