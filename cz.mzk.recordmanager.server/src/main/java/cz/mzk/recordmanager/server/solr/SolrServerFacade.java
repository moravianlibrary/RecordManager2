package cz.mzk.recordmanager.server.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

public interface SolrServerFacade {

	void add(Collection<SolrInputDocument> docs, int commitWithinMs) throws IOException,
			SolrServerException;

	void commit() throws SolrServerException, IOException;

	QueryResponse query(SolrQuery query) throws SolrServerException;

	void deleteById(List<String> ids) throws SolrServerException, IOException;

	void deleteByQuery(String query) throws SolrServerException, IOException;

	void deleteByQuery(String query, int commitWithinMs) throws SolrServerException, IOException;

}
