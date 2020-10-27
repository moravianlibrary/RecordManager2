package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import cz.mzk.recordmanager.server.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.io.FileNotFoundException;
import java.util.List;

public class SemanticEnrichmentReader implements ItemReader<List<SemanticEnrichment>> {

	private static final Logger logger = LoggerFactory.getLogger(SemanticEnrichmentReader.class);

	private final List<String> files;

	private SemanticEnrichmentStreamReader reader;

	public SemanticEnrichmentReader(String filename) throws FileNotFoundException {
		files = FileUtils.getFilesName(filename);
	}

	@Override
	public List<SemanticEnrichment> read() {
		if (!files.isEmpty()) {
			return parseRecords();
		}
		return null;
	}

	private List<SemanticEnrichment> parseRecords() {
		List<SemanticEnrichment> results;
		do {
			initializeInputStream();
			if (reader == null) return null;
			results = reader.next();
		} while (results.isEmpty());
		return results;
	}

	private void initializeInputStream() {
		while (!files.isEmpty()) {
			String fileName = files.remove(0);
			try {
				reader = new SemanticEnrichmentStreamReader(fileName);
				return;
			} catch (FileNotFoundException e) {
				logger.error("File {} not found.", fileName);
			}
		}
		reader = null;
	}

}
