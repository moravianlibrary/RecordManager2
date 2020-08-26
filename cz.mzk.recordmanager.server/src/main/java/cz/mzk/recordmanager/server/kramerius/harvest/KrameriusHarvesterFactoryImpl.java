package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;

public class KrameriusHarvesterFactoryImpl implements KrameriusHarvesterFactory {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SolrServerFactory solrServerFactory;

	@Override
	public KrameriusHarvester create(KrameriusHarvesterEnum type, KrameriusHarvesterParams parameters, Long confId,
			String inFile) {
		switch (type) {
		case SORTING:
			return new KrameriusHarvesterSorting(httpClient, solrServerFactory, parameters, confId);
		case FILE:
			return new KrameriusHarvesterFile(httpClient, solrServerFactory, parameters, confId, inFile);
		default:
			return new KrameriusHarvesterNoSorting(httpClient, solrServerFactory, parameters, confId);
		}
	}
}
