package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

public interface JobExecutor {

	Collection<String> getJobNames();

	Collection<JobParameterDeclaration> getParametersOfJob(String jobName);

	JobExecution execute(String jobName, JobParameters params, boolean forceRestart);

	JobExecution execute(String jobName, JobParameters parameters);

	JobExecution restart(Long jobExecutionId);

}
