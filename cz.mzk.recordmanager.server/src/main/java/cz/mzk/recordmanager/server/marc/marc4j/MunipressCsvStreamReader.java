package cz.mzk.recordmanager.server.marc.marc4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class MunipressCsvStreamReader implements MarcReader {

	private Record record;
	private MarcFactory factory = new MarcFactoryImpl();

	private Iterator<CSVRecord> iterator;

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public MunipressCsvStreamReader(InputStream input) {
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
		try {
			CSVParser parser = new CSVParser(new StringReader(IOUtils.toString(input, StandardCharsets.UTF_8)), CSVFormat.EXCEL);
			iterator = parser.iterator();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Returns the next record in the iteration.
	 *
	 * @return Record - the record object
	 */
	public Record next() {
		record = factory.newRecord();
		return record;
	}


}
