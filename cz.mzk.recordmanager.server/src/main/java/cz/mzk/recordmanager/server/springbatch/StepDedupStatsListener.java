package cz.mzk.recordmanager.server.springbatch;

import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StepDedupStatsListener extends StepProgressListener {

	private static final String executeSql = ResourceUtils.asString("job/dedupRecordsJob/dedup_stats.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	public StepDedupStatsListener() {
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		super.beforeStep(stepExecution);
		Map<String, Object> map = new HashMap<>();
		map.put("name", stepExecution.getStepName());
		map.put("job_execution_id", stepExecution.getJobExecutionId());
		map.put("time", stepExecution.getStartTime());
		map.put("type", "start");
		jdbcTemplate.update(executeSql, map);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		super.afterStep(stepExecution);
		Map<String, Object> map = new HashMap<>();
		map.put("name", stepExecution.getStepName());
		map.put("job_execution_id", stepExecution.getJobExecutionId());
		map.put("time", new Date());
		map.put("type", "end");
		jdbcTemplate.update(executeSql, map);
		return null;
	}

}
