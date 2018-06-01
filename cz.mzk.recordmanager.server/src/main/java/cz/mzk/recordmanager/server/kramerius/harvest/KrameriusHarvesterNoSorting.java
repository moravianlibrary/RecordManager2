package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KrameriusHarvesterNoSorting extends AbstractKrameriusHarvest {

	private static final Logger LOGGER = LoggerFactory.getLogger(KrameriusHarvesterNoSorting.class);

	private static final String PID_FIELD = "PID";

	private Long numFound = 0L;

	public KrameriusHarvesterNoSorting(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom);
	}

	@Override
	public List<String> getNextUuids() throws SolrServerException {
		if (getStart() > 0 && getStart() >= numFound) return null;

		SolrDocumentList documents = executeSolrQuery(getUuidQuery(PID_FIELD));
		numFound = documents.getNumFound();

		Long queryRows = params.getQueryRows() != null ? params.getQueryRows() : 10;
		setStart(getStart() + queryRows.intValue());

		return getUuids(documents, PID_FIELD);
	}

	private SolrQuery getUuidQuery(String... fields) throws SolrServerException {
		SolrQuery query = getBasicQuery(fields);

		if (getStart() != null) {
			query.setStart(getStart());
		}
		LOGGER.info("query: {}", query.getQuery());
		return query;
	}

}
