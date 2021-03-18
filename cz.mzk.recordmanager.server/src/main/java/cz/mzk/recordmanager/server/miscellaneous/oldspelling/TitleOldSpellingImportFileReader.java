package cz.mzk.recordmanager.server.miscellaneous.oldspelling;

import cz.mzk.recordmanager.server.model.TitleOldSpelling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TitleOldSpellingImportFileReader implements ItemReader<List<TitleOldSpelling>>, StepExecutionListener {

	private BufferedReader br = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(TitleOldSpellingImportFileReader.class);

	private final String FILENAME;

	private static final int BATCH_SIZE = 100;

	public TitleOldSpellingImportFileReader(String filename) {
		this.FILENAME = filename;
	}

	@Override
	public List<TitleOldSpelling> read() throws IOException {
		if (br == null) throw new IOException();
		if (br.ready()) return next();
		return null;
	}

	private List<TitleOldSpelling> next() throws IOException {
		List<TitleOldSpelling> result = new ArrayList<>();
		String newLine;

		while (br.ready()) {
			newLine = br.readLine();
			String[] split = newLine.split(";");
			if (split.length < 3 || split[0].equals(split[2])) continue;
			result.add(TitleOldSpelling.create(split[0], split[2]));
			if (result.size() < BATCH_SIZE) return result;
		}
		return result;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			LOGGER.info("Opening file: " + FILENAME);
			br = new BufferedReader(new FileReader(FILENAME));
		} catch (FileNotFoundException e) {
			LOGGER.info("Can't open file");
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}
}
