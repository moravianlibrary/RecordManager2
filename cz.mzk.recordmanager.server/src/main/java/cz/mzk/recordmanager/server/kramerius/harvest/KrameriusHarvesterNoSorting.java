package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static cz.mzk.recordmanager.server.kramerius.ApiMappingEnum.PID;

public class KrameriusHarvesterNoSorting extends KrameriusHarvesterImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(KrameriusHarvesterNoSorting.class);

	private Long numFound = 0L;

	private Integer start = 0;

	public KrameriusHarvesterNoSorting(HttpClient httpClient, SolrServerFactory solrServerFactory,
									   KrameriusHarvesterParams parameters, Long harvestedFrom) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom);
	}

	@Override
	public List<String> getNextUuids() throws SolrServerException {
		if (start > 0 && start >= numFound) return null;

		SolrDocumentList documents = executeSolrQuery(getUuidQuery(params.getApiMappingValue(PID)));
		numFound = documents.getNumFound();

		Long queryRows = params.getQueryRows() != null ? params.getQueryRows() : 10;
		start += queryRows.intValue();

		return getUuids(documents, params.getApiMappingValue(PID));
	}

	private SolrQuery getUuidQuery(String... fields) {
		SolrQuery query = getBasicQuery(fields);

		if (start != null) {
			query.setStart(start);
		}
		LOGGER.info("query: {}", query.getQuery());
		return query;
	}

}
