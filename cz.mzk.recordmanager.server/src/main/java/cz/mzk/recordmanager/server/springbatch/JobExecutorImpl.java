package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	public Long execute(String jobName, JobParameters params) {
		try {
			final Job job = jobRegistry.getJob(jobName);
			JobParametersIncrementer incrementer = job
					.getJobParametersIncrementer();
			if (incrementer != null) {
				params = incrementer.getNext(params);
			}
			job.getJobParametersValidator().validate(params);
			JobExecution exec = jobLauncher.run(job, params);
			return exec.getId();
		} catch (Exception ex) {
			throw new RuntimeException(String.format(
					"Job %s with parameters %s could not be started.", jobName,
					params), ex);
		}
	}

	@Override
	public void restart(Long jobExecutionId) {
		JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
		execution.setExitStatus(ExitStatus.FAILED);
		execution.setEndTime(new Date());
		jobRepository.update(execution);
		try {
			Job job = jobRegistry.getJob(execution.getJobInstance()
					.getJobName());
			execute(job.getName(), execution.getJobParameters());
		} catch (NoSuchJobException nsje) {
			throw new RuntimeException(String.format(
					"Job execution with id=%s could not be restarted.",
					jobExecutionId), nsje);
		}
	}

}
