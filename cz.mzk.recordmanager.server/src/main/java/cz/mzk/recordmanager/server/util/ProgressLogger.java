package cz.mzk.recordmanager.server.util;

import java.util.Calendar;

import org.slf4j.Logger;

public class ProgressLogger {

	private Logger logger;
	private int logPeriod;
	private int totalCount = 0;
	private long startTime = 0L;
	private static final int DEFAULT_STEP = 10000;

	public ProgressLogger(Logger logger, int logPeriod) {
		initialize(logger, logPeriod);
	}

	public ProgressLogger(Logger logger) {
		initialize(logger, DEFAULT_STEP);
	}

	public void initialize(Logger logger, int period) {
		this.logger = logger;
		logPeriod = period;
		startTime = Calendar.getInstance().getTimeInMillis();
	}

	public void incrementAndLogProgress() {
		increment();
		logProgress();
	}

	public void increment() {
		++totalCount;
	}

	public void logProgress() {
		if (totalCount % logPeriod == 0) {
			long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
			if (elapsedSecs == 0) elapsedSecs = 1;
			logger.info(String.format("Records: %,9d, processing speed %4d records/s",
					totalCount, totalCount / elapsedSecs));
		}
	}
}
