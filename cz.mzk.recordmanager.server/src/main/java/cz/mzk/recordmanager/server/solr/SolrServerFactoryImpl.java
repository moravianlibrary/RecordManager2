package cz.mzk.recordmanager.server.solr;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

public class SolrServerFactoryImpl implements SolrServerFactory {

	@Override
	public SolrServerFacade create(String url, SolrIndexingExceptionHandler exceptionHandler) {
		return new SolrServerFacadeImpl(new HttpSolrServer(url), exceptionHandler);
	}

	@Override
	public SolrServerFacade create(String url) {
		return create(url, RethrowingSolrIndexingExceptionHandler.INSTANCE);
	}

}
