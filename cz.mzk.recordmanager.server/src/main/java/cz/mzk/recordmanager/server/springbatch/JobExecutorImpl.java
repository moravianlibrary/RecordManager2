package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.OAIGranularity;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class JobExecutorImpl implements JobExecutor {

	private static Logger logger = LoggerFactory
			.getLogger(JobExecutorImpl.class);

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private JobRepository jobRepository;

	@Override
	public Collection<String> getJobNames() {
		return jobRegistry.getJobNames();
	}
	
	@Override
	public Collection<JobParameterDeclaration> getParametersOfJob(String jobName) {
		try {
			Job job = jobRegistry.getJob(jobName);
			JobParametersValidator validator = job.getJobParametersValidator();
			if (validator instanceof IntrospectiveJobParametersValidator) {
				return ((IntrospectiveJobParametersValidator) validator)
						.getParameters();
			} else {
				throw new IllegalArgumentException(String.format(
						"Job %s does not support introspection.", jobName));
			}
		} catch (NoSuchJobException nsje) {
			throw new IllegalArgumentException(String.format(
					"Job %s does not exist.", jobName));
		}
	}

	@Override
	public JobExecution execute(String jobName, JobParameters params) {
		return execute(jobName, params, false);
	}

	@Override
	public JobExecution execute(String jobName, JobParameters params, boolean forceRestart) {
		try {
			final Job job = jobRegistry.getJob(jobName);
			JobParameters transformedParams = transformJobParameters(jobName, params,
					job.getJobParametersValidator());
			job.getJobParametersValidator().validate(transformedParams);
			JobExecution lastExec = jobRepository.getLastJobExecution(jobName, transformedParams);
			if (forceRestart && lastExec != null && !lastExec.getStatus().equals(BatchStatus.COMPLETED)) {
				return restart(lastExec.getId());
			} else {
				logger.debug("Last execution: {}", lastExec);
				if (lastExec != null && lastExec.getStatus().equals(BatchStatus.COMPLETED)) {
					logger.debug("Job was already executed and completed");
					JobParametersIncrementer incrementer = job
							.getJobParametersIncrementer();
					if (incrementer != null) {
						logger.debug("About to increment parameters");
						transformedParams = incrementer.getNext(transformedParams);
					}
				}
				logger.debug("About to execute job");
				JobExecution exec = jobLauncher.run(job, transformedParams);
				JobExecution status = jobExplorer.getJobExecution(exec.getId());
				return status;
			}
		} catch (Exception ex) {
			throw new RuntimeException(String.format(
					"Job %s with parameters %s could not be started.", jobName,
					params), ex);
		}
	}

	@Override
	public JobExecution restart(Long jobExecutionId) {
		JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
		execution.setExitStatus(ExitStatus.FAILED);
		execution.setEndTime(new Date());
		jobRepository.update(execution);
		try {
			Job job = jobRegistry.getJob(execution.getJobInstance()
					.getJobName());
			return execute(job.getName(), execution.getJobParameters());
		} catch (NoSuchJobException nsje) {
			throw new RuntimeException(String.format(
					"Job execution with id=%s could not be restarted.",
					jobExecutionId), nsje);
		}
	}

	/**
	 * transform parameters values from String to required type; 
	 * @param inParams input {@link JobParameter}
	 * @param inValidator input {@link JobParametersValidator}
	 * @return transformed {@link JobParameter} with defined types 
	 * @throws JobParametersInvalidException when one of parameters can't be converted to required type
	 */
	private JobParameters transformJobParameters(final String jobName, final JobParameters inParams, 
			final JobParametersValidator inValidator) throws JobParametersInvalidException {
		
		if (!(inValidator instanceof IntrospectiveJobParametersValidator)) {
			throw new IllegalArgumentException(
					"Job %s does not support introspection.");
		}
		
		IntrospectiveJobParametersValidator insValidator = (IntrospectiveJobParametersValidator) inValidator;
		
		final Map<String, JobParameter> inParamMap = inParams.getParameters();
		final Map<String, JobParameter> transformedMap = new HashMap <String, JobParameter>();
		
		Map<String, JobParameterDeclaration> paramDeclMap = new HashMap<String, JobParameterDeclaration>();
		for (JobParameterDeclaration declaration: insValidator.getParameters()) {
			paramDeclMap.put(declaration.getName(), declaration);
		}
		
		//No need for declare 'repeat' parameter in job validator
		if (paramDeclMap.get(Constants.JOB_PARAM_REPEAT) == null) {
			paramDeclMap.put(Constants.JOB_PARAM_REPEAT, new JobParameterDeclaration(Constants.JOB_PARAM_REPEAT, ParameterType.LONG, false));
		}
		
		for (String key : inParamMap.keySet()) {
			JobParameterDeclaration declaration = paramDeclMap.get(key);
			if (declaration == null) {
				throw new JobParametersInvalidException("Unknown parameter: " + key);
			}
			Object originalValue = inParamMap.get(key).getValue();

			switch (declaration.getType()) {
			case DATE:
				if (originalValue instanceof Date) {
					transformedMap.put(key, new JobParameter(
							(Date) originalValue));
				} else {
					Date date = OAIGranularity.stringToDate((String) originalValue);
					if (date != null) {
						transformedMap.put(key, new JobParameter(date));
					} else {
						throw new JobParametersInvalidException("Parameter "
								+ key + " isn't in valid format.");
					}
				}
				break;
				
			case DOUBLE:
				if (originalValue instanceof Double) {
					transformedMap.put(key, new JobParameter(
							(Double) originalValue));
				} else {
					transformedMap.put(
							key,
							new JobParameter(Double
									.valueOf((String) originalValue)));
				}
				break;
				
			case LONG:
				if (originalValue instanceof Long) {
					transformedMap.put(key, new JobParameter(
							(Long) originalValue));
				} else {
					transformedMap.put(
							key,
							new JobParameter(Long
									.valueOf((String) originalValue)));
				}
				break;
				
			case STRING:
				if (originalValue instanceof String) {
					transformedMap.put(key, new JobParameter(
							(String) originalValue));
				} else {
					transformedMap.put(
							key,
							new JobParameter(originalValue.toString()));
				}
			default:
				break;
			}
		}
		
		// compute incremental 'from' 
		if (transformedMap.containsKey(Constants.JOB_PARAM_INCREMENTAL)) {
			JobParameter param = transformedMap.get(Constants.JOB_PARAM_INCREMENTAL);
			if (param.getValue().equals(1L)) {
				Long importConfId = null;
				if (transformedMap.containsKey(Constants.JOB_PARAM_CONF_ID)) {
					JobParameter tempParam = transformedMap.get(Constants.JOB_PARAM_CONF_ID);
					if (tempParam.getType().equals(JobParameter.ParameterType.LONG)) {
						importConfId = (Long) tempParam.getValue();
					}
				}
				
				Date incrementalFrom = getIncrementalStartingDate(jobName, importConfId);
				if (incrementalFrom != null) {
					if (transformedMap.containsKey(Constants.JOB_PARAM_FROM_DATE)) {
						logger.warn("Overriding given 'from' parameter.");
					}
					transformedMap.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(incrementalFrom));
					logger.info("Detected incremental 'from' parameter: " + incrementalFrom);
				}
			}
		}
		
		JobParameter repeatParam = transformedMap.get(Constants.JOB_PARAM_REPEAT);
		if (repeatParam != null && (new Long(1)).equals(repeatParam.getValue())) {
			transformedMap.put(Constants.JOB_PARAM_TIMESTAMP, new JobParameter(new Date()));
		}
		transformedMap.remove(Constants.JOB_PARAM_REPEAT);
		return new JobParameters(transformedMap);
	}
	
	/**
	 * get last date when job having given name was successfully completed
	 * @param jobName
	 * @param importConfId
	 * @return
	 */
	private Date getIncrementalStartingDate(String jobName, Long importConfId) {
		Date maxEndTime = null;
		for (JobInstance ins: jobExplorer.getJobInstances(jobName, 0, 100000)) {
			for (JobExecution exec: jobExplorer.getJobExecutions(ins)) {
				// consider competed jobs only
				if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
					continue;
				}
				// consider jobs having equal importConfiguration only
				Long execImportConf = exec.getJobParameters().getLong(Constants.JOB_PARAM_CONF_ID, 0L);
				if (importConfId != null && !importConfId.equals(execImportConf)) {
					continue;
				}
				
				Date currentEndTime = exec.getEndTime();
				if (maxEndTime == null || currentEndTime.after(maxEndTime)) {
					maxEndTime = currentEndTime;
				}
			}
		}
		return maxEndTime;
	}

}
