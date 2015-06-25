package cz.mzk.recordmanager.server.solr;

import org.apache.solr.client.solrj.SolrServer;

public interface SolrServerFactory {
	
	public SolrServer create(String url);

}
