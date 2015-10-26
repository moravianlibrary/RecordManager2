package cz.mzk.recordmanager.server.solr;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

public class SolrServerFactoryImpl implements SolrServerFactory {

	@Override
	public SolrServerFacade create(String url) {
		return new SolrServerFacadeImpl(new HttpSolrServer(url));
	}

}
