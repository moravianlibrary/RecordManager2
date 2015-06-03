package cz.mzk.recordmanager.server.dedup;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class StepProgressListener implements StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(StepProgressListener.class);
	
	private String stepName;
	
	long startTime = 0L;
	
	public StepProgressListener(String stepName) {
		this.stepName = stepName;
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		startTime = Calendar.getInstance().getTimeInMillis();
		logger.info(String.format("Step %s started.", stepName));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
		logger.info(String.format("Step %s finished with status %s. Execution took %d seconds", stepName, stepExecution.getStatus(), elapsedSecs));
		return null;
	}

}
