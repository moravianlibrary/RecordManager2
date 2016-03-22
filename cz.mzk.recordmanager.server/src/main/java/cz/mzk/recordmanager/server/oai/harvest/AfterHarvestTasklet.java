package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.util.Constants;

public class AfterHarvestTasklet implements Tasklet {

	private static Logger logger = LoggerFactory.getLogger(AfterHarvestTasklet.class);

	private static final String UPDATE_QUERY = "UPDATE harvested_record SET deleted = :deleted WHERE " +
			" import_conf_id = :importConfId and updated < :executed";

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		JobParameters params = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getJobParameters();
		Long configId = params.getLong(Constants.JOB_PARAM_CONF_ID);
		Date started = params.getDate(Constants.JOB_PARAM_START_TIME);
		if (started == null) {
			return RepeatStatus.FINISHED;
		}
		Map<String, Object> updateParams = ImmutableMap.of(
				"deleted", new Date(), //
				"importConfId", configId, //
				"executed", started
		);
		int updated = jdbcTemplate.update(UPDATE_QUERY, updateParams);
		logger.info("{} harvested records updated before '{}' mark as deleted " +
				"for import_conf_id={}", updated, started, configId);
		return RepeatStatus.FINISHED;
	}

}
