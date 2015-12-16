package cz.mzk.recordmanager.server.imports;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
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

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.marc4j.MarcAlephStreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.MarcISO2709StreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.MarcLineStreamReader;
import cz.mzk.recordmanager.server.marc.marc4j.MarcXmlReader;
import cz.mzk.recordmanager.server.marc.marc4j.PatentsXmlStreamReader;

public class ImportRecordsFileReader implements ItemReader<List<Record>> {

	private static Logger logger = LoggerFactory.getLogger(ImportRecordsFileReader.class);
	
	private MarcReader reader;
	
	private IOFormat format;
	
	private FileInputStream inStream;
	
	private int batchSize = 20;

	public ImportRecordsFileReader(String filename, String strFormat) throws FileNotFoundException {
		format = IOFormat.stringToExportFormat(strFormat);
		inStream = new FileInputStream(filename);
		reader = getMarcReader(inStream);
	}
	
	@Override
	public List<Record> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		List<Record> batch = new ArrayList<Record>();
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
		default:
			return new MarcXmlReader(inStream);
		}
	}

}
