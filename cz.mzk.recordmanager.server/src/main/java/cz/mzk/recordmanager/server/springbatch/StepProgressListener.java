package cz.mzk.recordmanager.server.springbatch;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class StepProgressListener implements StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(StepProgressListener.class);

	long startTime = 0L;

	public StepProgressListener() {
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		startTime = Calendar.getInstance().getTimeInMillis();
		logger.info(String.format("Step %s started.", stepExecution.getStepName()));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
		logger.info(String.format("Step %s finished with status %s. Execution took %d seconds", stepExecution.getStepName(), stepExecution.getStatus(), elapsedSecs));
		return null;
	}

}
