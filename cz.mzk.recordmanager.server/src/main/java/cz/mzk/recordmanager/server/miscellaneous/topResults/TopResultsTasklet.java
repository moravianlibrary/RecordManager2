package cz.mzk.recordmanager.server.miscellaneous.topResults;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationType;
import cz.mzk.recordmanager.server.model.InspirationName;
import cz.mzk.recordmanager.server.oai.dao.InspirationNameDAO;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TopResultsTasklet implements Tasklet {

	private static final String executeSql = ResourceUtils.asString("job/topResults/topResults.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private InspirationNameDAO inspirationNameDAO;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		InspirationName inspirationName = inspirationNameDAO.getOrCreate("top_results", InspirationType.TOP_RESULTS);
		Map<String, Object> map = new HashMap<>();
		map.put("year", Calendar.getInstance().get(Calendar.YEAR) - 1);
		map.put("dedupCount", 50);
		map.put("results", 20);
		map.put("inspiration_name_id", inspirationName.getId());
		jdbcTemplate.update(executeSql, map);
		return RepeatStatus.FINISHED;
	}

}
