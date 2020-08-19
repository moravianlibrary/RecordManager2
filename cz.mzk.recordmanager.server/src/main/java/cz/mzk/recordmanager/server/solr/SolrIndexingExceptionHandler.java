package cz.mzk.recordmanager.server.solr;

import java.util.Collection;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public interface SolrIndexingExceptionHandler {

	enum Action {
		SKIP,
		RETRY,
		FALLBACK
	}

	Action handle(Exception ex, Collection<SolrInputDocument> documents) throws SolrServerException;

	Action handle(Exception ex, String query) throws SolrServerException;

	default void ok() {
	}

}
