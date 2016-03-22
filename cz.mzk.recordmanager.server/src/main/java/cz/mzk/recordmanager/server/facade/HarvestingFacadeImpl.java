package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class HarvestingFacadeImpl implements HarvestingFacade {

	private static Logger logger = LoggerFactory.getLogger(HarvestingFacadeImpl.class);

	private final String lastCompletedHarvestQuery = ResourceUtils.asString("sql/query/LastCompletedHarvestQuery.sql");

	private final String lastCompletedReharvestQuery = ResourceUtils.asString("sql/query/LastCompletedReharvestQuery.sql");

	private final String lastJobExecutionQuery = ResourceUtils.asString("sql/query/LastJobExecutionQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void incrementalHarvest(OAIHarvestConfiguration conf) {
		LocalDateTime lastHarvestTime = getLastHarvest(conf);
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(conf.getId()));
		if (lastHarvestTime != null) {
			Date lastHarvest = Date.from(lastHarvestTime.atZone(ZoneId.systemDefault()).toInstant());
			logger.trace("Starting harvest from {}", lastHarvest);
			parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(lastHarvest));
		}
		parameters.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(new Date()));
		parameters.put(Constants.JOB_PARAM_START_TIME, new JobParameter(new Date()));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(getJobName(conf), params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Incremental harvest failed", exec);
		}
	}

	@Override
	public void fullHarvest(OAIHarvestConfiguration conf) {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(conf.getId()));
		parameters.put(Constants.JOB_PARAM_START_TIME, new JobParameter(new Date()));
		parameters.put(Constants.JOB_PARAM_REHARVEST, new JobParameter(Constants.JOB_PARAM_TRUE_VALUE));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(getJobName(conf), params);
	}

	@Override
	public LocalDateTime getLastFullHarvest(OAIHarvestConfiguration conf) {
		return query(lastCompletedReharvestQuery, ImmutableMap.of("jobName", getJobName(conf),
				Constants.JOB_PARAM_CONF_ID, conf.getId()));
	}

	@Override
	public LocalDateTime getLastHarvest(OAIHarvestConfiguration conf) {
		return query(lastCompletedHarvestQuery, ImmutableMap.of("jobName", getJobName(conf),
				Constants.JOB_PARAM_CONF_ID, conf.getId()));
	}

	@Override
	public void obalkyKnihHarvest() {
		jobExecutor.execute(Constants.JOB_ID_HARVEST_OBALKY_KNIH, new JobParameters());
	}

	@Override
	public LocalDateTime getLastObalkyKnihHarvest() {
		return query(lastJobExecutionQuery, ImmutableMap.of("jobName", Constants.JOB_ID_HARVEST_OBALKY_KNIH));
	}

	private LocalDateTime query(String query, Map<String, ?> params) {
		List<Date> lastIndex = jdbcTemplate.queryForList(query, params, Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? LocalDateTime.ofInstant(lastIndex.get(0).toInstant(), ZoneId.systemDefault()) : null;
	}

	private String getJobName(OAIHarvestConfiguration conf) {
		return MoreObjects.firstNonNull(conf.getHarvestJobName(), Constants.JOB_ID_HARVEST);
	}

}
