package cz.mzk.recordmanager.server.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public enum RethrowingSolrIndexingExceptionHandler implements
		SolrIndexingExceptionHandler {

	INSTANCE;

	@Override
	public Action handle(Exception ex,
			Collection<SolrInputDocument> documents)
			throws SolrServerException {
		return rethrow(ex);
	}

	@Override
	public Action handle(Exception ex, String query) throws SolrServerException {
		return rethrow(ex);
	}

	protected Action rethrow(Exception ex) throws SolrServerException {
		if (ex instanceof SolrServerException) {
			throw (SolrServerException) ex;
		} else {
			throw new SolrServerException(ex);
		}
	}

}
