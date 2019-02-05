package cz.mzk.recordmanager.server.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
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
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
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

	@Autowired
	private OAIHarvestConfigurationDAO harvestConfigurationDAO;

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	@Autowired
	private DownloadImportConfigurationDAO downloadImportConfigurationDAO;

	@Autowired
	private ImportRecordFacadeImpl importRecordFacade;
	@Override
	public void incrementalHarvest(OAIHarvestConfiguration conf) {
		incrementalHarvest(conf.getId(), getJobName(conf));
	}
	
	@Override
	public void incrementalHarvest(KrameriusConfiguration conf) {
		incrementalHarvest(conf.getId(), getJobName(conf));
	}

	@Override
	public void incrementalHarvest(Long id) {
		OAIHarvestConfiguration configuration = harvestConfigurationDAO.get(id);
		if (configuration != null) {
			incrementalHarvest(configuration);
		} else {
			KrameriusConfiguration krameriusConfiguration = krameriusConfigurationDAO.get(id);
			if (krameriusConfiguration != null){
				incrementalHarvest(krameriusConfiguration);
			}
		}
	}

	public void incrementalHarvest(long conf_id, String job_name){
		LocalDateTime lastHarvestTime = getLastHarvest(conf_id, job_name);
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(conf_id));
		if (lastHarvestTime != null) {
			Date lastHarvest = Date.from(lastHarvestTime.atZone(ZoneId.systemDefault()).toInstant());
			logger.trace("Starting harvest from {}", lastHarvest);
			parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(lastHarvest));
		}
		parameters.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(new Date()));
		parameters.put(Constants.JOB_PARAM_START_TIME, new JobParameter(new Date()));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(job_name, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Incremental harvest failed", exec);
		}
	}

	public void fullHarvest(long id, String jobName){
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(id));
		parameters.put(Constants.JOB_PARAM_START_TIME, new JobParameter(new Date()));
		parameters.put(Constants.JOB_PARAM_REHARVEST, new JobParameter(Constants.JOB_PARAM_TRUE_VALUE));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(jobName, params);
	}

	@Override
	public void fullHarvest(OAIHarvestConfiguration conf) {
		fullHarvest(conf.getId(), getJobName(conf));
	}

	@Override
	public void fullHarvest(KrameriusConfiguration conf) {
		fullHarvest(conf.getId(), getJobName(conf));
	}

	@Override
	public void fullHarvest(Long id) {
		OAIHarvestConfiguration configuration = harvestConfigurationDAO.get(id);
		if (configuration != null) {
			fullHarvest(configuration);
		} else {
			KrameriusConfiguration krameriusConfiguration = krameriusConfigurationDAO.get(id);

			if (krameriusConfiguration != null) {
				fullHarvest(krameriusConfiguration);
			} else {
				DownloadImportConfiguration downloadImportConfiguration =  downloadImportConfigurationDAO.get(id);
				if (downloadImportConfiguration != null){
					importRecordFacade.downloadAndImportRecordSJob(downloadImportConfiguration);
				}
			}
		}
	}

	@Override
	public LocalDateTime getLastFullHarvest(OAIHarvestConfiguration conf) {
		return getLastFullHarvest(conf.getId(), getJobName(conf));
	}
	
	@Override
	public LocalDateTime getLastFullHarvest(KrameriusConfiguration conf) {
		return getLastFullHarvest(conf.getId(), getJobName(conf));
	}
	
	public LocalDateTime getLastFullHarvest(long conf_id, String job_name) {
		return query(lastCompletedReharvestQuery, ImmutableMap.of("jobName", job_name,
				Constants.JOB_PARAM_CONF_ID, conf_id));
	}

	@Override
	public LocalDateTime getLastHarvest(OAIHarvestConfiguration conf) {
		return getLastHarvest(conf.getId(), getJobName(conf));
	}
	
	@Override
	public LocalDateTime getLastHarvest(KrameriusConfiguration conf) {
		return getLastHarvest(conf.getId(), getJobName(conf));
	}
	
	public LocalDateTime getLastHarvest(long conf_id, String job_name) {
		return query(lastCompletedHarvestQuery, ImmutableMap.of("jobName", job_name,
				Constants.JOB_PARAM_CONF_ID, conf_id));
	}

	@Override
	public void obalkyKnihHarvest() {
		jobExecutor.execute(Constants.JOB_ID_HARVEST_OBALKY_KNIH, new JobParameters());
	}

	@Override
	public void incrementalObalkyKnihTocHarvest() {
		Map<String, JobParameter> parameters = new HashMap<>();
		LocalDateTime ldt = query(lastJobExecutionQuery, ImmutableMap.of("jobName", Constants.JOB_ID_HARVEST_OBALKY_KNIH));
		if (ldt != null)
			parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())));
		jobExecutor.execute(Constants.JOB_ID_HARVEST_OBALKY_KNIH, new JobParameters(parameters));
	}

	@Override
	public LocalDateTime getLastObalkyKnihHarvest() {
		return query(lastJobExecutionQuery, ImmutableMap.of("jobName", Constants.JOB_ID_HARVEST_OBALKY_KNIH));
	}

	@Override
	public void incrementalFulltextJob(KrameriusConfiguration conf) {
		incrementalHarvest(conf.getId(), Constants.JOB_ID_FULLTEXT_KRAMERIUS);
	}

	@Override
	public void incrementalFulltextJob(Long id) {
		KrameriusConfiguration krameriusConfiguration = krameriusConfigurationDAO.get(id);
		if (krameriusConfiguration != null){
			incrementalFulltextJob(krameriusConfiguration);
		}
	}

	private LocalDateTime query(String query, Map<String, ?> params) {
		List<Date> lastIndex = jdbcTemplate.queryForList(query, params, Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? LocalDateTime.ofInstant(lastIndex.get(0).toInstant(), ZoneId.systemDefault()) : null;
	}

	private String getJobName(OAIHarvestConfiguration conf) {
		return MoreObjects.firstNonNull(conf.getHarvestJobName(), Constants.JOB_ID_HARVEST);
	}
	
	private String getJobName(KrameriusConfiguration conf) {
		return MoreObjects.firstNonNull(conf.getHarvestJobName(), Constants.JOB_ID_HARVEST_KRAMERIUS);
	}

	@Override
	public void incrementalObalkyKnihAnnotationsJob() {
		Map<String, JobParameter> parameters = new HashMap<>();
		LocalDateTime ldt = query(lastJobExecutionQuery, ImmutableMap.of("jobName", Constants.JOB_ID_IMPORT_ANNOTATIONS));
		if (ldt != null)
			parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())));
		jobExecutor.execute(Constants.JOB_ID_IMPORT_ANNOTATIONS, new JobParameters(parameters));
	}
}
