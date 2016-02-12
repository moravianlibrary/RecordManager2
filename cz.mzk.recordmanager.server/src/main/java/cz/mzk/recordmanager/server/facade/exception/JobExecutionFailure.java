package cz.mzk.recordmanager.server.facade.exception;

import org.springframework.batch.core.JobExecution;

public class JobExecutionFailure extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final JobExecution jobExecution;

	public JobExecutionFailure(String msg, JobExecution jobExecution) {
		super(msg);
		this.jobExecution = jobExecution;
	}

	public JobExecution getJobExecution() {
		return jobExecution;
	}

}
