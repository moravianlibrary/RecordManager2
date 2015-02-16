package cz.mzk.recordmanager.server.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

public class SolrServerFactoryImpl implements SolrServerFactory {

	@Override
	public SolrServer create(String url) {
		return new HttpSolrServer(url);
	}

}
