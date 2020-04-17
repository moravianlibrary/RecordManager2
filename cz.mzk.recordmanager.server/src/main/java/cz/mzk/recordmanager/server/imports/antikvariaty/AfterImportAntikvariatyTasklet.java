package cz.mzk.recordmanager.server.imports.antikvariaty;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AfterImportAntikvariatyTasklet implements Tasklet {

	private static Logger logger = LoggerFactory.getLogger(AfterImportAntikvariatyTasklet.class);

	private static final String UPDATED_QUERY = "UPDATE dedup_record SET updated = localtimestamp WHERE id in (" +
			"SELECT dedup_record_id FROM antikvariaty_url_view WHERE updated > :lastExecution)";
	private static final String LAST_HARVEST_QUERY = "UPDATE dedup_record SET updated = localtimestamp WHERE id in (" +
			"SELECT dedup_record_id FROM antikvariaty_url_view WHERE last_harvest < :started)";
	private static final String DELETE_QUERY = "DELETE FROM antikvariaty where last_harvest < :started";

	private final String lastJobExecutionQuery = ResourceUtils.asString("sql/query/LastJobExecutionQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution,
								ChunkContext chunkContext) throws Exception {
		Date started = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getCreateTime();
		Date lastExecution = lastCompletedExecution(Constants.JOB_ID_IMPORT_ANTIKVARIATY);
		if (lastExecution == null) lastExecution = new Date(0);

		int updated;
		Map<String, Object> updateParams;

		updateParams = ImmutableMap.of("lastExecution", lastExecution);
		updated = jdbcTemplate.update(UPDATED_QUERY, updateParams);
		logger.info("{} antikvariaty records updated after '{}' will be reindexed", updated, lastExecution);

		updateParams = ImmutableMap.of("started", started);
		updated = jdbcTemplate.update(LAST_HARVEST_QUERY, updateParams);
		logger.info("{} antikvariaty records deleted from last import ({}) will be reindexed", updated, lastExecution);

		updateParams = ImmutableMap.of("started", started);
		updated = jdbcTemplate.update(DELETE_QUERY, updateParams);
		logger.info("delete {} antikvariaty records", updated);

		return RepeatStatus.FINISHED;
	}

	private Date lastCompletedExecution(String jobName) {
		List<Date> lastIndex = jdbcTemplate.queryForList(
				lastJobExecutionQuery, ImmutableMap.of("jobName", jobName), Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? Date.from(lastIndex.get(0).toInstant()) : null;
	}


}
