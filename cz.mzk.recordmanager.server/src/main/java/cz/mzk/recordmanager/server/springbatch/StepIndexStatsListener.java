package cz.mzk.recordmanager.server.springbatch;

import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StepIndexStatsListener implements StepExecutionListener {

	private static final String executeSql = ResourceUtils.asString("job/index/index_stats.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	public StepIndexStatsListener() {
	}

	private Date getDate(StepExecution stepExecution, String param) {
		if (stepExecution.getJobParameters().getDate(param) != null) {
			return stepExecution.getJobParameters().getDate(param);
		}
		return new Date();
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		Map<String, Object> map = new HashMap<>();
		map.put("job_execution_id", stepExecution.getJobExecutionId());
		map.put("from", getDate(stepExecution, Constants.JOB_PARAM_FROM_DATE));
		map.put("to", getDate(stepExecution, Constants.JOB_PARAM_UNTIL_DATE));
		jdbcTemplate.update(executeSql, map);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
