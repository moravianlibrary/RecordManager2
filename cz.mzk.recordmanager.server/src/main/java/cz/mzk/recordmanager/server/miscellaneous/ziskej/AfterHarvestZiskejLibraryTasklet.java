package cz.mzk.recordmanager.server.miscellaneous.ziskej;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AfterHarvestZiskejLibraryTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(AfterHarvestZiskejLibraryTasklet.class);

	private static final String UPDATED_QUERY = "UPDATE dedup_record SET updated = localtimestamp WHERE id in (" +
			"SELECT dedup_record_id FROM harvested_record WHERE sigla IN (SELECT sigla FROM ziskej_library WHERE " +
			"updated > :lastExecution))";
	private static final String LAST_HARVEST_QUERY = "UPDATE dedup_record SET updated = localtimestamp WHERE " +
			"id in (SELECT dedup_record_id FROM harvested_record WHERE sigla IN (" +
			"SELECT sigla FROM ziskej_library WHERE last_harvest < :started))";
	private static final String DELETE_QUERY = "DELETE FROM ziskej_library where last_harvest < :started";

	private final String lastJobExecutionQuery =
			ResourceUtils.asString("sql/query/LastCompletedZiskejLibraryQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		JobParameters params = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getJobParameters();
		Long configId = params.getLong(Constants.JOB_PARAM_CONF_ID);
		Date started = chunkContext.getStepContext().getStepExecution().getJobExecution().getCreateTime();
		Date lastExecution = lastCompletedExecution(Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES);
		if (lastExecution == null) lastExecution = new Date(0);

		int updated;
		Map<String, Object> updateParams;

		updateParams = ImmutableMap.of("lastExecution", lastExecution, "confId", configId);
		updated = jdbcTemplate.update(UPDATED_QUERY, updateParams);
		logger.info("{} libaries updated after ({}) ", updated, lastExecution);

		updateParams = ImmutableMap.of("started", started, "confId", configId);
		updated = jdbcTemplate.update(LAST_HARVEST_QUERY, updateParams);
		logger.info("{} libaries deleted from last harvest ({}) will be reindexed",
				updated, lastExecution);

		updateParams = ImmutableMap.of("started", started, "confId", configId);
		updated = jdbcTemplate.update(DELETE_QUERY, updateParams);
		logger.info("delete {} libaries", updated);

		return RepeatStatus.FINISHED;
	}

	private Date lastCompletedExecution(String jobName) {
		List<Date> lastIndex = jdbcTemplate.queryForList(
				lastJobExecutionQuery, ImmutableMap.of("jobName", jobName), Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? Date.from(lastIndex.get(0).toInstant()) : null;
	}
}
