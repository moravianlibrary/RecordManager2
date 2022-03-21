package cz.mzk.recordmanager.server.solr;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaultTolerantIndexingExceptionHandler implements SolrIndexingExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(FaultTolerantIndexingExceptionHandler.class);

	private static final List<String> RETRYABLE_ERRORS = Arrays.asList(
			"Server refused connection",
			"IOException occured when talking to server",
			"Java heap space",
			"Error from server",
			"Expected mime type"
	);

	private static final List<String> SKIPPABLE_ERRORS = Arrays.asList(
			"multiple values encountered for non multiValued field",
			"Error adding field",
			"Couldn't parse shape",
			"Document contains at least one immense term"
	);

	private final long failureTimeout = 600_000L;

	private final long sleepBetweenFailures = 60_000L;

	private final int maxSkippableFailures = 100;

	private volatile AtomicLong lastFailureTime = new AtomicLong(0L);

	private int skippableFailuresCount = 0;

	@Override
	public Action handle(Exception ex, Collection<SolrInputDocument> documents)
			throws SolrServerException {
		if (documents.size() == 1 && skippable(ex)) {
			if (maxSkippableFailures > 0 && skippableFailuresCount >= maxSkippableFailures) {
				logger.error("Limit {} on number of skipped failures exceeded, failing", maxSkippableFailures);
				rethrow(ex);
			}
			skippableFailuresCount++;
			logger.warn("About to skip indexing of {} due to error {}", documents.iterator().next(), ex);
			return Action.SKIP;
		} else if (documents.size() > 1 && skippable(ex)) {
			logger.warn("Fallbacking to index one record at time due to error", ex);
			return Action.FALLBACK;
		}
		long now = System.currentTimeMillis();
		lastFailureTime.compareAndSet(0L, now);
		if (retryable(ex)) {
			long lastFailure = lastFailureTime.get();
			if (lastFailure != 0 && now - lastFailure > failureTimeout) {
				rethrow(ex);
			}
			try {
				logger.warn("Retryable exeption thrown, retrying in {} ms", sleepBetweenFailures, ex);
				Thread.sleep(sleepBetweenFailures);
				return Action.RETRY;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return rethrow(ex);
	}

	@Override
	public Action handle(Exception ex, String query) throws SolrServerException {
		if (skippable(ex)) {
			if (maxSkippableFailures > 0 && skippableFailuresCount >= maxSkippableFailures) {
				logger.error("Limit {} on number of skipped failures exceeded, failing", maxSkippableFailures);
				rethrow(ex);
			}
			skippableFailuresCount++;
			logger.warn("About to skip query {} due to error {}", query, ex);
			return Action.SKIP;
		}
		long now = System.currentTimeMillis();
		lastFailureTime.compareAndSet(0L, now);
		if (retryable(ex)) {
			long lastFailure = lastFailureTime.get();
			if (lastFailure != 0 && now - lastFailure > failureTimeout) {
				rethrow(ex);
			}
			try {
				logger.warn("Retryable exeption thrown, retrying in {} ms", sleepBetweenFailures, ex);
				Thread.sleep(sleepBetweenFailures);
				return Action.RETRY;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return rethrow(ex);
	}

	@Override
	public void ok() {
		lastFailureTime.set(0L);
	}

	protected boolean retryable(Exception ex) {
		boolean isSolrException = (ex instanceof SolrServerException) || (ex instanceof SolrException);
		return isSolrException && RETRYABLE_ERRORS.stream().anyMatch(mess -> ex.getMessage().contains(mess));
	}

	protected boolean skippable(Exception ex) {
		boolean isSolrException = (ex instanceof SolrServerException) || (ex instanceof SolrException);
		return isSolrException && SKIPPABLE_ERRORS.stream().anyMatch(mess -> ex.getMessage().contains(mess));
	}

	protected Action rethrow(Exception ex) throws SolrServerException {
		if (ex instanceof SolrServerException) {
			throw (SolrServerException) ex;
		} else {
			throw new SolrServerException(ex);
		}
	}

}
