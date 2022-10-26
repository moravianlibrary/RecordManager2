package cz.mzk.recordmanager.server.imports.inspirations;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationMappingFieldDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.Map;

public class InspirationCleanTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(InspirationCleanTasklet.class);

	private static final String UPDATE_QUERY = "UPDATE dedup_record SET updated = localtimestamp " +
			"WHERE id in (select dedup_record_id from harvested_record where id in (" +
			"SELECT harvested_record_id FROM inspiration WHERE " +
			"(last_harvest < :executed OR updated > :executed) AND inspiration_name_id in (" +
			"select id FROM inspiration_name WHERE type = :type)))";

	private static final String DELETE_QUERY = "DELETE FROM inspiration WHERE " +
			"last_harvest < :executed AND inspiration_name_id in (" +
			"select id FROM inspiration_name WHERE type = :type)";

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
		Map<String, Object> updateParams = ImmutableMap.of(
				"executed", started,
				"type", type.getValue()
		);
		jdbcTemplate.update(UPDATE_QUERY, updateParams);
		int updated = jdbcTemplate.update(DELETE_QUERY, updateParams);
		logger.info("{} inspirations updated before '{}' mark as deleted", updated, started);
	}

}
