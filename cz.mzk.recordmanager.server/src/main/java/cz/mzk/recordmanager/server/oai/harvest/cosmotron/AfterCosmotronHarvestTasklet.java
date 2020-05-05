package cz.mzk.recordmanager.server.oai.harvest.cosmotron;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.oai.harvest.AfterHarvestTasklet;
import cz.mzk.recordmanager.server.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Date;
import java.util.Map;

public class AfterCosmotronHarvestTasklet extends AfterHarvestTasklet implements Tasklet {

	private static Logger logger = LoggerFactory.getLogger(AfterCosmotronHarvestTasklet.class);

	private static final String UPDATE_QUERY = "UPDATE cosmotron_996 " +
			"SET deleted = :deleted, updated = :deleted " +
			"WHERE import_conf_id = :importConfId AND last_harvest < :executed AND deleted is null";

	@Override
	public RepeatStatus execute(StepContribution contribution,
								ChunkContext chunkContext) throws Exception {
		super.execute(contribution, chunkContext);
		JobParameters params = chunkContext.getStepContext().getStepExecution().getJobExecution()
				.getJobParameters();
		Long configId = params.getLong(Constants.JOB_PARAM_CONF_ID);
		Date started = chunkContext.getStepContext().getStepExecution().getJobExecution().getCreateTime();
		if (started == null) {
			return RepeatStatus.FINISHED;
		}
		Map<String, Object> updateParams = ImmutableMap.of(
				"deleted", new Date(), //
				"importConfId", configId, //
				"executed", started
		);
		int updated = jdbcTemplate.update(UPDATE_QUERY, updateParams);
		logger.info("{} cosmotron 996 records updated before '{}' mark as deleted " +
				"for import_conf_id={}", updated, started, configId);
		return RepeatStatus.FINISHED;
	}

}
