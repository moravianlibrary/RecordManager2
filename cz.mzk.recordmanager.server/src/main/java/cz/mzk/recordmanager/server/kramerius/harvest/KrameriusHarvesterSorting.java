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

import static cz.mzk.recordmanager.server.kramerius.ApiMappingEnum.PID;

public class KrameriusHarvesterSorting extends KrameriusHarvesterImpl {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(KrameriusHarvesterSorting.class);

	private String lastPid = "";

	public KrameriusHarvesterSorting(HttpClient httpClient, SolrServerFactory solrServerFactory,
									 KrameriusHarvesterParams parameters, Long harvestedFrom) {
		super(httpClient, solrServerFactory, parameters, harvestedFrom);
	}

	@Override
	public List<String> getNextUuids() throws SolrServerException {
		if (lastPid == null) return null;

		SolrDocumentList documents = executeSolrQuery(getUuidQuery(params.getApiMappingValue(PID)));
		List<String> uuids = getUuids(documents, params.getApiMappingValue(PID));

		String nextUuid = Iterables.getLast(uuids, null);
		if (lastPid.equals(nextUuid)) lastPid = null;
		else lastPid = nextUuid;

		return uuids;
	}

	private SolrQuery getUuidQuery(String... fields) {
		SolrQuery query = getBasicQuery(fields);

		if (lastPid != null && !lastPid.isEmpty()) {
			query.add("fq", SolrUtils.createFieldQuery(params.getApiMappingValue(PID), SolrUtils.createRange(lastPid, null)));
		}
		query.setSort(params.getApiMappingValue(PID), ORDER.asc);
		LOGGER.info("nextPid: {}", lastPid);

		return query;
	}

}
