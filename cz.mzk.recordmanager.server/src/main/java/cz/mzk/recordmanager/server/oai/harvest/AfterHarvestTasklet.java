package cz.mzk.recordmanager.server.oai.harvest;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.util.Constants;
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
import java.util.Map;

public class AfterHarvestTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(AfterHarvestTasklet.class);

	private static final String UPDATE_QUERY = "UPDATE harvested_record " +
			"SET deleted = :deleted, updated = :deleted " +
			"WHERE import_conf_id = :importConfId AND last_harvest < :executed AND deleted is null";

	@Autowired
	protected NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private MappingResolver propertyResolver;

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		JobParameters params = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getJobParameters();
		Long configId = params.getLong(Constants.JOB_PARAM_CONF_ID);
		Date started = chunkContext.getStepContext().getStepExecution().getJobExecution().getCreateTime();
		if (started == null) {
			return RepeatStatus.FINISHED;
		}
		updateRecords(configId, started);
		try {
			propertyResolver.resolve("source/" + configId + ".map").getMapping()
					.forEach((key, value) -> updateRecords(Long.parseLong(key), started));
		} catch (Exception ignore) {
		}
		return RepeatStatus.FINISHED;
	}

	private void updateRecords(Long confId, Date started) {
		Map<String, Object> updateParams = ImmutableMap.of(
				"deleted", new Date(), //
				"importConfId", confId, //
				"executed", started
		);
		int updated = jdbcTemplate.update(UPDATE_QUERY, updateParams);
		logger.info("{} harvested records updated before '{}' mark as deleted " +
				"for import_conf_id={}", updated, started, confId);
	}

}
