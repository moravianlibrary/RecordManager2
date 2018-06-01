package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.kramerius.FedoraModels;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KrameriusHarvesterNoSorting extends AbstractKrameriusHarvest {

	private static final Logger LOGGER = LoggerFactory.getLogger(KrameriusHarvesterNoSorting.class);

	private static final String PID_FIELD = "PID";

	private Long numFound = 0L;

	public KrameriusHarvesterNoSorting(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom);
	}

	public List<String> getUuids(Integer krameriusStart) throws SolrServerException {
		List<String> uuids = new ArrayList<>();

		SolrDocumentList documents = sendRequest(krameriusStart, PID_FIELD);
		numFound = documents.getNumFound();

		for (SolrDocument document : documents) {
			for (Object pid : document.getFieldValues(PID_FIELD)) {
				uuids.add(pid.toString());
			}
		}
		return uuids;
	}

	private SolrDocumentList sendRequest(Integer start, String... fields) throws SolrServerException {
		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");

		//works with all possible models in single configuration
		String harvestedModelsStatement = String.join(" OR ", FedoraModels.HARVESTED_MODELS);
		query.add("fq",  harvestedModelsStatement);

		if (params.getFrom() != null || params.getUntil() != null) {
			String range = SolrUtils.createDateRange(params.getFrom(), params.getUntil());
			query.add("fq", SolrUtils.createFieldQuery("modified_date", range));
		}

		query.setFields(fields);
		query.setRows((params.getQueryRows() != null) ? params.getQueryRows().intValue() : 10);
		query.setTimeAllowed(MAX_TIME_ALLOWED);

		if (start != null) {
			query.setStart(start);
		}
		LOGGER.info("query: {}", query.getQuery());

		return executeSolrQuery(query);
	}

	public Long getNumFound() {
		return this.numFound;
	}

}
