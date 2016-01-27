package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

public interface JobExecutor {

	public Collection<String> getJobNames();

	public Collection<JobParameterDeclaration> getParametersOfJob(String jobName);

	public JobExecution execute(String jobName, JobParameters params, boolean forceRestart);

	public JobExecution execute(String jobName, JobParameters parameters);

	public JobExecution restart(Long jobExecutionId);

}
