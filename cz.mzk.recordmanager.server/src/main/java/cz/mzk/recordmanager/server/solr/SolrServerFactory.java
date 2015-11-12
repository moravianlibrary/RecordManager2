package cz.mzk.recordmanager.server.solr;

import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl.Mode;

public interface SolrServerFactory {

	public SolrServerFacade create(String url);

	public SolrServerFacade create(String url, Mode mode);

	public SolrServerFacade create(String url, Mode mode,
			SolrIndexingExceptionHandler exceptionHandler);

}
