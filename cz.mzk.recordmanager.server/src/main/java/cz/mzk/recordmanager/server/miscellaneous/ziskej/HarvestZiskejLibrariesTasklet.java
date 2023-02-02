package cz.mzk.recordmanager.server.miscellaneous.ziskej;

import cz.mzk.recordmanager.server.oai.dao.SiglaAllDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HarvestZiskejLibrariesTasklet implements Tasklet {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SiglaAllDAO siglaAllDAO;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	private final String[] formats;

	private static final Logger logger = LoggerFactory.getLogger(HarvestZiskejLibrariesTasklet.class);

	public static final Map<String, String> URLS = new HashMap<>();

	static {
		URLS.put("ziskej", "https://ziskej.techlib.cz:9080/api/v1/libraries?service=mvs");
		URLS.put("ziskej_edd", "https://ziskej.techlib.cz:9080/api/v1/libraries?service=edd");
	}

	private static final String UPDATE_DATESTAMP_QUERY = "UPDATE dedup_record SET updated=localtimestamp WHERE id IN (" +
			"SELECT dedup_record_id FROM harvested_record WHERE sigla IN (:siglas))";

	private static final String UPDATE_PARTICIPATION_QUERY = "UPDATE sigla_all SET %s=NOT %s " +
			"WHERE sigla in (:siglas)";

	public HarvestZiskejLibrariesTasklet(String format) {
		this.formats = format.split(",");
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		for (String format : formats) {
			logger.info(String.format("Format: %s", format));
			JSONObject obj = new JSONObject(harvest(format));

			List<String> apilist = new ArrayList<>();
			JSONArray jsonArray = obj.getJSONArray("items");
			if (jsonArray != null) {
				int len = jsonArray.length();
				for (int i = 0; i < len; i++) {
					apilist.add(jsonArray.get(i).toString());
				}
			}
			Set<String> dbList = siglaAllDAO.getParticipatingSigla(format);
			Collection<String> union = CollectionUtils.union(apilist, dbList);
			Collection<String> intersection = CollectionUtils.intersection(apilist, dbList);
			List<String> uniq = new ArrayList<>(union);
			uniq.removeAll(intersection);

			if (!uniq.isEmpty()) {
				Map<String, Object> updateParams = new HashMap<>();
				int updated;

				updateParams.put("siglas", uniq);
				updated = jdbcTemplate.update(UPDATE_DATESTAMP_QUERY, updateParams);
				logger.info("{} libaries updated", updated);
				updated = jdbcTemplate.update(String.format(UPDATE_PARTICIPATION_QUERY, format, format), updateParams);
				logger.info("{} libaries updated", updated);
			}
		}

		return RepeatStatus.FINISHED;
	}

	protected String harvest(String format) throws RuntimeException {
		if (!URLS.containsKey(format))
			throw new RuntimeException(String.format("Available formats: %s", URLS.keySet()));
		String url = URLS.get(format);
		try (InputStream is = httpClient.executeGet(url)) {
			logger.info("Downloading: " + url);
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Could not download ziskej list");
			throw new RuntimeException();
		}
	}

}
