package cz.mzk.recordmanager.server.imports.inspirations;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class InspirationImportFileReader implements ItemReader<Map<String, List<String>>>, StepExecutionListener {

	@Autowired
	private HttpClient httpClient;

	@Value(value = "${vufind.url:#{null}}")
	private String vufindUrl;

	private BufferedReader br = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(InspirationImportFileReader.class);

	private final String FILENAME;

	private static final String URL_SUFFIX = "AJAX/JSON?method=harvestWidgetsContents";
	private static final Pattern PATTERN_NAME = Pattern.compile("^\\[([^]]*)]$");
	private static final Pattern PATTERN_ID = Pattern.compile("^[^.]*\\..*");

	public InspirationImportFileReader(String filename) throws FileNotFoundException {
		this.FILENAME = filename;
	}

	@Override
	public Map<String, List<String>> read() throws IOException {
		if (br == null) throw new IOException();
		if (br.ready()) return next();
		return null;
	}

	private Map<String, List<String>> next() throws IOException {
		Map<String, List<String>> result = new HashMap<>();
		String name = null;
		Set<String> ids = new HashSet<>();
		Matcher matcher;
		String newLine;

		while (br.ready()) {
			newLine = br.readLine();

			if (newLine.isEmpty() && name != null) {
				break;
			}
			matcher = PATTERN_NAME.matcher(newLine);
			if (matcher.matches()) {
				name = matcher.group(1);
			}
			matcher = PATTERN_ID.matcher(newLine);
			if (matcher.matches()) {
				ids.add(newLine);
			}
		}
		result.put(name, new ArrayList<>(ids));
		return result;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			String url = vufindUrl + URL_SUFFIX;
			if (FILENAME == null) {
				LOGGER.info("Harvesting inspirations list from: " + url);
				br = new BufferedReader(new InputStreamReader(httpClient.executeGet(url)));
			} else {
				LOGGER.info("Opening file: " + FILENAME);
				br = new BufferedReader(new FileReader(new File(FILENAME)));
			}
		} catch (IOException e) {
			LOGGER.info("Can't harvest inspiration list");
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}
}
