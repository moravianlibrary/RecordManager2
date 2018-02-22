package cz.mzk.recordmanager.server.imports.classifier;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.batch.item.ItemReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class ClassifierImportFileReader implements ItemReader<PredictedRecord> {

	private Iterator<CSVRecord> iterator;

	public ClassifierImportFileReader(String filename) throws FileNotFoundException {
		initializeReader(filename);
	}

	@Override
	public PredictedRecord read() throws IOException {
		if (iterator.hasNext()) return next();
		return null;
	}

	private void initializeReader(String filename) {
		try {
			Reader in = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8);
			CSVParser parser = CSVFormat.EXCEL.withHeader().parse(in);
			iterator = parser.iterator();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PredictedRecord next() throws IOException {
		CSVRecord rec = iterator.next();
		return PredictedRecord.create(rec);
	}


}
