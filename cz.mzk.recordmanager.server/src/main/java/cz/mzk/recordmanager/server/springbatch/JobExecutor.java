package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;

import org.springframework.batch.core.JobParameters;

public interface JobExecutor {

	public Collection<String> getJobNames();

	public Collection<JobParameterDeclaration> getParametersOfJob(String jobName);

	public Long execute(String jobName, JobParameters params, boolean forceRestart);

	public Long execute(String jobName, JobParameters parameters);

	public void restart(Long jobExecutionId); 

}
