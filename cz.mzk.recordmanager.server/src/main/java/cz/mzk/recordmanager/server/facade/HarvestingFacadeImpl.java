package cz.mzk.recordmanager.server.facade;

import java.util.Date;
import java.util.HashMap;
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

	private final String lastJobExecutionQuery = ResourceUtils.asString("sql/query/LastJobExecutionQuery.sql");

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void incrementalHarvest(OAIHarvestConfiguration conf) {
		Date lastHarvest = getLastHarvest(conf);
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(conf.getId()));
		if (lastHarvest != null) {
			logger.trace("Starting harvest from {}", lastHarvest);
			parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(lastHarvest));
		}
		parameters.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(new Date()));
		parameters.put(Constants.JOB_PARAM_START_TIME, new JobParameter(new Date()));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_HARVEST, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Incremental harvest failed", exec);
		}
	}

	@Override
	public void fullHarvest(OAIHarvestConfiguration conf) {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(conf.getId()));
		parameters.put(Constants.JOB_PARAM_START_TIME, new JobParameter(new Date()));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(Constants.JOB_ID_REHARVEST, params);
	}

	@Override
	public Date getLastHarvest(OAIHarvestConfiguration conf) {
		return jdbcTemplate.queryForObject(lastCompletedHarvestQuery, //
				ImmutableMap.of("jobName", Constants.JOB_ID_HARVEST, Constants.JOB_PARAM_CONF_ID, conf.getId()), Date.class);
	}

	@Override
	public void obalkyKnihHarvest() {
		jobExecutor.execute(Constants.JOB_ID_HARVEST_OBALKY_KNIH, new JobParameters());
	}

	@Override
	public Date getLastObalkyKnihHarvest() {
		return jdbcTemplate.queryForObject(lastJobExecutionQuery, //
				ImmutableMap.of("jobName", Constants.JOB_ID_HARVEST_OBALKY_KNIH), Date.class);
	}

}
