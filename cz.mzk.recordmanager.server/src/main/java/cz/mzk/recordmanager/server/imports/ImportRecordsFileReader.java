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

	private static final Logger logger = LoggerFactory.getLogger(ImportRecordsFileReader.class);

	@Autowired
	private ImportConfigurationDAO configDao;

	@Autowired
	private DownloadImportConfigurationDAO dicDao;

	@Autowired
	private HttpClient httpClient;

	private MarcReader reader;

	private IOFormat format;

	private final Long confId;

	private static final int BATCH_SIZE = 20;

	private List<String> files = null;

	public ImportRecordsFileReader(Long confId, String filename, String strFormat) throws FileNotFoundException {
		format = IOFormat.stringToExportFormat(strFormat);
		this.confId = confId;
		getFilesName(filename);
	}

	public ImportRecordsFileReader(Long confId) {
		this.confId = confId;
		this.reader = null;
	}

	@Override
	public synchronized List<Record> read() throws Exception {
		List<Record> results = new ArrayList<>();
		try {
			if (reader == null)
				if (files == null) initializeDownloadReader();
				else initializeFilesReader();
			else if (!reader.hasNext()) initializeFilesReader();
			while (reader.hasNext()) {
				try {
					results.add(reader.next());
					if (results.size() >= BATCH_SIZE) {
						break;
					}
					if (!reader.hasNext()) initializeFilesReader();
				} catch (MarcException e) {
					logger.debug(e.getMessage());
					initializeFilesReader();
				}
			}
		} catch (MarcException e) {
			logger.debug(e.getMessage());
		}
		return results.isEmpty() ? null : results;
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
		case OSOBNOSTI_REGIONU:
			return new OsobnostiRegionuXmlStreamReader(inStream);
		case SFX:
			return new SfxJibXmlStreamReader(inStream, configDao.get(confId).getIdPrefix());
		case SFX_NLK:
			return new SfxJibNlkCsvStreamReader(inStream, configDao.get(confId).getIdPrefix());
		case MUNIPRESS:
			return new MunipressCsvStreamReader(inStream);
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
		if (files != null && !files.isEmpty()) {
			do {
				try {
					FileInputStream inStream = new FileInputStream(files.remove(0));
					reader = getMarcReader(inStream);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
			} while (!reader.hasNext());
		}
	}

	private void getFilesName(String filename) throws FileNotFoundException {
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

}
