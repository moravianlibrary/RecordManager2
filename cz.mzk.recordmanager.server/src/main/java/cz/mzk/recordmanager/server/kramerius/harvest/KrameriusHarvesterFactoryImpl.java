package cz.mzk.recordmanager.server.kramerius.harvest;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusHarvesterFactoryImpl implements KrameriusHarvesterFactory {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SolrServerFactory solrServerFactory;

	private static final String SORTING = "sorting";

	@Override
	public KrameriusHarvester create(String type, KrameriusHarvesterParams parameters, Long confId) {
		switch (type.toLowerCase()) {
		case SORTING:
			return new KrameriusHarvesterSorting(httpClient, solrServerFactory, parameters, confId);
		default:
			return new KrameriusHarvesterNoSorting(httpClient, solrServerFactory, parameters, confId);
		}
	}
}
