package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class SqlCommandTasklet implements Tasklet {

	private static Logger logger = LoggerFactory
			.getLogger(JobExecutorImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String[] commands;

	public SqlCommandTasklet(Collection<String> commands) {
		this.commands = commands.toArray(new String[commands.size()]);
	}

	public SqlCommandTasklet(String... commands) {
		this.commands = commands;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		ExecutionContext ctx = chunkContext.getStepContext().getStepExecution().getExecutionContext();
		int index = 0;
		if (ctx.containsKey("index")) {
			index = ctx.getInt("index");
		}
		if (index >= commands.length) {
			return RepeatStatus.FINISHED;
		}
		String sql = commands[index];
		logger.debug("Before execute sql: {}", sql);
		jdbcTemplate.execute(sql);
		logger.debug("After execute sql: {}", sql);
		index++;
		ctx.putInt("index", index);
		return (index >= commands.length)? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
	}

}
