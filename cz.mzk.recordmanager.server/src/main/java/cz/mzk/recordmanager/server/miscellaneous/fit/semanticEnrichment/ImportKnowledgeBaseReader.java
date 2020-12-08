package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import cz.mzk.recordmanager.server.model.FitKnowledgeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ImportKnowledgeBaseReader implements ItemReader<List<FitKnowledgeBase>> {

	private static Logger logger = LoggerFactory.getLogger(ImportKnowledgeBaseReader.class);

	private static final int BATCH_SIZE = 100;

	private final BufferedReader reader;

	private Long lineCounter = 0L;

	public ImportKnowledgeBaseReader(String filename) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(new File(filename)));
	}

	@Override
	public List<FitKnowledgeBase> read() throws Exception {
		List<FitKnowledgeBase> results = new ArrayList<>();
		while (reader.ready()) {
			lineCounter++;
			String kbValue = parseRecord(reader.readLine());
			if (kbValue == null) continue;
			results.add(FitKnowledgeBase.create(lineCounter, kbValue));
			if (lineCounter % BATCH_SIZE == 0) return results;
		}
		return results.isEmpty() ? null : results;
	}

	private String parseRecord(String line) {
		String[] data = line.split("\\t");
		return data[2];
	}

}
