package cz.mzk.recordmanager.server.solr;

import java.util.Collection;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public interface SolrIndexingExceptionHandler {

	public enum Action {
		SKIP,
		RETRY,
		FALLBACK;
	}

	public Action handle(Exception ex, Collection<SolrInputDocument> documents) throws SolrServerException;

	default public void ok() {
	}

}
