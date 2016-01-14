package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.collect.ImmutableMap;

public class AfterHarvestTasklet implements Tasklet {

	private static final String UPDATE_QUERY = "UPDATE harvested_record SET deleted = :deleted WHERE " +
			" import_conf_id = :importConfId and updated < :executed";

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	private final Long configId;

	private final boolean reharvest;

	public AfterHarvestTasklet(Long configId, Date from, Date to) {
		this.configId = configId;
		this.reharvest = (from == null && to == null);
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		if (!reharvest) {
			return RepeatStatus.FINISHED;
		}
		// FIXME: is this OK when the job failed and was then restarted?
		Date started = (Date) chunkContext.getStepContext().getStepExecution().getJobExecution().getStartTime();
		Map<String, Object> params = ImmutableMap.of(
				"deleted", new Date(), //
				"importConfId", configId, //
				"executed", started
		);
		jdbcTemplate.update(UPDATE_QUERY, params);
		return RepeatStatus.FINISHED;
	}

}
