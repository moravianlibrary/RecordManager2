package cz.mzk.recordmanager.server.solr;

public interface SolrServerFactory {
	
	public SolrServerFacade create(String url);

}
