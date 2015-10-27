package cz.mzk.recordmanager.server.solr;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public interface SolrIndexingExceptionHandler {

	public boolean handle(Exception ex, Collection<SolrInputDocument> documents) throws SolrServerException;

}
