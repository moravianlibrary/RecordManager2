package cz.mzk.recordmanager.server.solr;

import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl.Mode;

public interface SolrServerFactory {

	SolrServerFacade create(String url);

	SolrServerFacade create(String url, Mode mode);

	SolrServerFacade create(String url, Mode mode,
							SolrIndexingExceptionHandler exceptionHandler);

}
