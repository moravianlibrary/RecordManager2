package cz.mzk.recordmanager.server.springbatch;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SqlCommandTasklet implements Tasklet {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private List<String> commands;

	public SqlCommandTasklet() {
		
	}
	
	public void setCommand(String command) {
		this.commands = Collections.singletonList(command);
	}
	
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		for (String sql : commands) {
			jdbcTemplate.execute(sql);
		}
		this.commands = Collections.emptyList();
		return RepeatStatus.FINISHED;
	}

}
