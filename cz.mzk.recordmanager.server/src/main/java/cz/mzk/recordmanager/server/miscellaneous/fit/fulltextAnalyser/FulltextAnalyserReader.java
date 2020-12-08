package cz.mzk.recordmanager.server.miscellaneous.fit.fulltextAnalyser;

import cz.mzk.recordmanager.server.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FulltextAnalyserReader implements ItemReader<List<FulltextAnalyser>> {

	private static final Logger logger = LoggerFactory.getLogger(FulltextAnalyserReader.class);

	private final List<String> files;

	private FulltextAnalyserStreamReader reader;

	public FulltextAnalyserReader(String filename) throws FileNotFoundException {
		files = FileUtils.getFilesName(filename);
	}

	@Override
	public List<FulltextAnalyser> read() throws Exception {
		if (!files.isEmpty()) {
			return parseRecords();
		}
		return null;
	}

	private List<FulltextAnalyser> parseRecords() {
		List<FulltextAnalyser> results = new ArrayList<>();
		initializeInputStream();
		if (reader == null) return results;
		FulltextAnalyser data = reader.next();
		results.add(data);
		return results;
	}

	private void initializeInputStream() {
		while (!files.isEmpty()) {
			String fileName = files.remove(0);
			try {
				reader = new FulltextAnalyserStreamReader(fileName);
				return;
			} catch (FileNotFoundException e) {
				logger.error("File {} not found.", fileName);
			}
		}
		reader = null;
	}

}
