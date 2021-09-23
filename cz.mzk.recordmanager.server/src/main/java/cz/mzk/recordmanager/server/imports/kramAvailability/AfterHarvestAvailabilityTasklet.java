package cz.mzk.recordmanager.server.imports.kramAvailability;

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

import static cz.mzk.recordmanager.server.imports.kramAvailability.KramAvailabilityHarvestType.TITLES;

public class AfterHarvestAvailabilityTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(AfterHarvestAvailabilityTasklet.class);

	private static final String UPDATED_QUERY = "UPDATE dedup_record SET updated = localtimestamp WHERE id in (" +
			"SELECT dedup_record_id FROM kram_availability_view WHERE import_conf_id = :confId AND " +
			"updated > :lastExecution)";
	private static final String LAST_HARVEST_QUERY = "UPDATE dedup_record SET updated = localtimestamp WHERE " +
			"id IN (SELECT dedup_record_id FROM kram_availability_view WHERE import_conf_id = :confId AND " +
			"last_harvest < :started AND type %s IN ('page','periodicalitem','periodicalvolume'))";
	private static final String DELETE_QUERY = "DELETE FROM kram_availability WHERE import_conf_id = :confId " +
			"AND last_harvest < :started AND type %s IN ('page','periodicalitem','periodicalvolume')";

	private final String lastJobExecutionQuery =
			ResourceUtils.asString("sql/query/LastCompletedKramAvailabilityQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		JobParameters params = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getJobParameters();
		Long configId = params.getLong(Constants.JOB_PARAM_CONF_ID);
		KramAvailabilityHarvestType type = KramAvailabilityHarvestType.fromValue(params.getString(Constants.JOB_PARAM_TYPE));
		Date started = chunkContext.getStepContext().getStepExecution().getJobExecution().getCreateTime();
		Date lastExecution = lastCompletedExecution(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY, configId);
		if (lastExecution == null) lastExecution = new Date(0);

		updateTitles(configId, started, lastExecution, type);

		return RepeatStatus.FINISHED;
	}

	private Date lastCompletedExecution(String jobName, Long confId) {
		List<Date> lastIndex = jdbcTemplate.queryForList(
				lastJobExecutionQuery, ImmutableMap.of("jobName", jobName, "configurationId", confId), Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? Date.from(lastIndex.get(0).toInstant()) : null;
	}

	private void updateTitles(Long configId, Date started, Date lastExecution, KramAvailabilityHarvestType type) {
		int updated;
		String sqlTypeCondition = type.equals(TITLES) ? "NOT" : "";
		Map<String, Object> updateParams;
		updateParams = ImmutableMap.of("lastExecution", lastExecution, "confId", configId);
		updated = jdbcTemplate.update(UPDATED_QUERY, updateParams);
		logger.info("{} availabilities from source {} updated after '{}' ", updated, configId, lastExecution);

		updateParams = ImmutableMap.of("started", started, "confId", configId);
		updated = jdbcTemplate.update(String.format(LAST_HARVEST_QUERY, sqlTypeCondition), updateParams);
		logger.info("{} availabilities deleted from source {} from last import ({}) will be reindexed",
				updated, configId, lastExecution);

		updateParams = ImmutableMap.of("started", started, "confId", configId);
		updated = jdbcTemplate.update(String.format(DELETE_QUERY, sqlTypeCondition), updateParams);
		logger.info("delete {} availabilities from source {}", updated, configId);
	}

}
