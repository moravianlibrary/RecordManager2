package cz.mzk.recordmanager.server.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public enum RethrowingSolrIndexingExceptionHandler implements
		SolrIndexingExceptionHandler {

	INSTANCE;

	@Override
	public boolean handle(Exception ex,
			Collection<SolrInputDocument> documents)
			throws SolrServerException {
		if (ex instanceof SolrServerException) {
			throw (SolrServerException) ex;
		} else {
			throw new SolrServerException(ex);
		}
	}

}
