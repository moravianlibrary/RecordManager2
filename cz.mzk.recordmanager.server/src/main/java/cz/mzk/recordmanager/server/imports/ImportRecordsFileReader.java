package cz.mzk.recordmanager.server.imports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.marc4j.MarcAlephStreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.MarcISO2709StreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.MarcLineStreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.MarcXmlReader;
import cz.mzk.recordmanager.server.marc.marc4j.OsobnostiRegionuXmlStreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.PatentsXmlStreamReader;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;

public class ImportRecordsFileReader implements ItemReader<List<Record>> {

	private static Logger logger = LoggerFactory.getLogger(ImportRecordsFileReader.class);
	
	@Autowired
	private DownloadImportConfigurationDAO dicDao;
	
	@Autowired 
	private HttpClient httpClient;
	
	private MarcReader reader;
	
	private IOFormat format;
	
	private FileInputStream inStream;
	
	private Long confId;
	
	private int batchSize = 20;
	
	private Deque<String> files = null;

	private String pathName = null;
	
	public ImportRecordsFileReader(String filename, String strFormat) throws FileNotFoundException {
		format = IOFormat.stringToExportFormat(strFormat);
		getFilesName(filename);
		initializeFilesReader();
	}
	
	public ImportRecordsFileReader(Long confId) throws Exception {
		this.confId = confId;
		this.reader = null;
	}
	
	@Override
	public List<Record> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		List<Record> batch = new ArrayList<Record>();
		
		if (reader == null) initializeDownloadReader();
		else if (!reader.hasNext()) initializeFilesReader();
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
		return batch.isEmpty() ? null : batch;
	}
	
	protected MarcReader getMarcReader(InputStream inStream) {
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
		default:
			return new MarcXmlReader(inStream);
		}
	}
	
	protected void initializeDownloadReader() throws IOException{
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

	protected void initializeFilesReader() {
		try {
			if(!files.isEmpty()){
				String file = pathName+files.pop();
				inStream = new FileInputStream(file);
				reader = getMarcReader(inStream);
			}
		} catch (FileNotFoundException e) {
			logger.warn(e.getMessage());
		}
	}
	
	protected void getFilesName(String filename) {
		files = new ArrayDeque<String>();
		File f = new File(filename);
		if (f.isFile()) {
			pathName = f.getParent()+"/";
			files.push(f.getName());
		}
		else {
			for (File file: f.listFiles()) {
				pathName = file.getParent()+"/";
				files.push(file.getName());
			}
		}
	}

}
