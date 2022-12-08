package cz.mzk.recordmanager.server.imports.inspirations;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationMappingFieldDAO;
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

public class InspirationCleanTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(InspirationCleanTasklet.class);

	private static final String UPDATE_QUERY = "UPDATE dedup_record SET updated = :now " +
			"WHERE id in (select dedup_record_id from harvested_record where id in (" +
			"SELECT harvested_record_id FROM harvested_record_inspiration WHERE " +
			"(last_harvest < :executed OR updated > :executed) AND inspiration_id in (" +
			"select id FROM inspiration WHERE type = :type)))";

	private static final String DELETE_QUERY = "DELETE FROM harvested_record_inspiration WHERE " +
			"last_harvest < :executed AND inspiration_id in (" +
			"select id FROM inspiration WHERE type = :type)";

	private final String lastJobExecutionQuery = ResourceUtils.asString("sql/query/LastJobExecutionEndTimeQuery.sql");

	@Autowired
	protected NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	protected ImportConfigurationMappingFieldDAO importConfigurationMappingFieldDAO;

	private final InspirationType type;

	InspirationCleanTasklet(InspirationType type) {
		this.type = type;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		Date started = chunkContext.getStepContext().getStepExecution().getJobExecution().getCreateTime();
		if (started == null) {
			return RepeatStatus.FINISHED;
		}
		updateRecords(started);
		return RepeatStatus.FINISHED;
	}

	private void updateRecords(Date started) {
		Date lastCompleted = query(lastJobExecutionQuery, ImmutableMap.of("jobName", Constants.JOB_ID_IMPORT_INSPIRATION));
		Map<String, Object> updateParams = ImmutableMap.of(
				"now", new Date(),
				"executed", lastCompleted,
				"type", type.getValue()
		);
		int updated = jdbcTemplate.update(UPDATE_QUERY, updateParams);
		logger.info("{} inspirations updated", updated);
		updateParams = ImmutableMap.of(
				"executed", started,
				"type", type.getValue()
		);
		updated = jdbcTemplate.update(DELETE_QUERY, updateParams);
		logger.info("{} inspirations updated before '{}' deleted", updated, started);
	}

	private Date query(String query, Map<String, ?> params) {
		List<Date> lastIndex = jdbcTemplate.queryForList(query, params, Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? lastIndex.get(0) : new Date(0);
	}

}
