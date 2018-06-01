package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.kramerius.FedoraModels;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KrameriusHarvester extends AbstractKrameriusHarvest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(KrameriusHarvester.class);

	private static String PID_FIELD = "PID";

	public KrameriusHarvester(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom);
	}

	public List<String> getUuids(String nextPid) throws SolrServerException {
		List<String> uuids = new ArrayList<>();

		SolrDocumentList documents = sendRequest(nextPid, PID_FIELD);

		for (SolrDocument document : documents) {
			for (Object pid : document.getFieldValues(PID_FIELD)) {
				uuids.add(pid.toString());
			}
		}
		return uuids;
	}

	private SolrDocumentList sendRequest(String nextPid, String... fields) throws SolrServerException {
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



		if (nextPid != null) {
			query.add("fq", SolrUtils.createFieldQuery(PID_FIELD, SolrUtils.createRange(nextPid, null)));
		}
		query.setSort(PID_FIELD, ORDER.asc);
		LOGGER.info("nextPid: {}", nextPid);

		return executeSolrQuery(query);
	}

}
