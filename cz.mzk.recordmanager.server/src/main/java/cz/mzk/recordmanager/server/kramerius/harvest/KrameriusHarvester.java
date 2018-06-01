package cz.mzk.recordmanager.server.kramerius.harvest;

import com.google.common.collect.Iterables;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KrameriusHarvester extends AbstractKrameriusHarvest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(KrameriusHarvester.class);

	private static String PID_FIELD = "PID";

	public KrameriusHarvester(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom);
	}

	@Override
	public List<String> getNextUuids() throws SolrServerException {
		SolrDocumentList documents = executeSolrQuery(getUuidQuery(PID_FIELD));

		if (documents.isEmpty()) return null;

		List<String> uuids = getUuids(documents, PID_FIELD);
		setNextPid(Iterables.getLast(uuids, null));

		return uuids;
	}

	private SolrQuery getUuidQuery(String... fields) throws SolrServerException {
		SolrQuery query = getBasicQuery(fields);

		if (getNextPid() != null) {
			query.add("fq", SolrUtils.createFieldQuery(PID_FIELD, SolrUtils.createRange(getNextPid(), null)));
		}
		query.setSort(PID_FIELD, ORDER.asc);
		LOGGER.info("nextPid: {}", getNextPid());

		return query;
	}

}
