package cz.mzk.recordmanager.server.imports.inspirations;

import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class InspirationImportFileReader implements ItemReader<Map<String, List<String>>>, StepExecutionListener {

	@Autowired
	private HttpClient httpClient;

	@Value(value = "${vufind.url:#{null}}")
	private String vufindUrl;

	private JSONArray data = null;
	private int i = 0;

	private static final Logger LOGGER = LoggerFactory.getLogger(InspirationImportFileReader.class);

	private final String FILENAME;

	private static final String URL_SUFFIX = "AJAX/JSON?method=harvestWidgetsContents";

	public InspirationImportFileReader(String filename) {
		this.FILENAME = filename;
	}

	@Override
	public Map<String, List<String>> read() throws IOException {
		if (i < data.length()) return next();
		return null;
	}

	private Map<String, List<String>> next() {
		Map<String, List<String>> result = new HashMap<>();
		Set<String> ids = new HashSet<>();

		JSONObject inspiration = data.getJSONObject(i++);

		JSONArray items = inspiration.getJSONArray("items");
		if (items != null) {
			int len = items.length();
			for (int i = 0; i < len; i++) {
				ids.add(items.get(i).toString());
			}
		}
		result.put(inspiration.get("list").toString(), new ArrayList<>(ids));
		return result;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			JSONObject obj;
			String url = vufindUrl + URL_SUFFIX;
			if (FILENAME == null) {
				LOGGER.info("Harvesting inspirations list from: " + url);
				obj = new JSONObject(IOUtils.toString(httpClient.executeGet(url), StandardCharsets.UTF_8));
			} else {
				LOGGER.info("Opening file: " + FILENAME);
				obj = new JSONObject(new String(Files.readAllBytes(Paths.get(FILENAME)), StandardCharsets.UTF_8));
			}
			data = obj.getJSONArray("data");
		} catch (IOException e) {
			LOGGER.info("Can't harvest inspiration list");
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
