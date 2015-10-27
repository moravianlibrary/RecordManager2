package cz.mzk.recordmanager.server.solr;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LoggingSolrIndexingExceptionHandler implements SolrIndexingExceptionHandler {

	INSTANCE;

	private static Logger logger = LoggerFactory.getLogger(LoggingSolrIndexingExceptionHandler.class);

	@Override
	public boolean handle(Exception ex, Collection<SolrInputDocument> documents) {
		if (documents.size() == 1) {
			logger.error(String.format("Exception thrown during indexing record: %s", documents.iterator().next()), ex);
		}
		return true; // fallback to index one record at time
	}

}
