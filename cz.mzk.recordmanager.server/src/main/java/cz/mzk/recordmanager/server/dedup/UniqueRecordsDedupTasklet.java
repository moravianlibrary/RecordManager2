package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;

public class UniqueRecordsDedupTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(UniqueRecordsDedupTasklet.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final String command;

	public UniqueRecordsDedupTasklet(String command) {
		super();
		this.command = command;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		ExecutionContext ctx = chunkContext.getStepContext().getStepExecution().getExecutionContext();
		Long nextHarvestedRecordId = DataAccessUtils.singleResult(jdbcTemplate.query(command,
				new Object[]{}, new LongValueRowMapper()));
		if (nextHarvestedRecordId != null) {
			ctx.putLong("harvestedRecordId", nextHarvestedRecordId);
		}
		logger.debug("nextHarvestedRecordId: {}", nextHarvestedRecordId);
		return (nextHarvestedRecordId == null) ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
	}

}
