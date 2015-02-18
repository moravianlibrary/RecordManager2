package cz.mzk.recordmanager.server.springbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import cz.mzk.recordmanager.server.index.IndexRecordsToSolrJobConfig;

public enum JobFailureListener implements JobExecutionListener {
	
	INSTANCE;

	private static Logger logger = LoggerFactory.getLogger(IndexRecordsToSolrJobConfig.class);
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			ExitStatus exitStatus = ExitStatus.FAILED;
			for (Throwable ex : jobExecution.getAllFailureExceptions()) {
				logger.debug("Exeption thrown when executing job", ex);
				exitStatus = exitStatus.addExitDescription(ex);
			}
			jobExecution.setExitStatus(exitStatus);
		}
	}

}