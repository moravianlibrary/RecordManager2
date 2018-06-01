package cz.mzk.recordmanager.server.kramerius.harvest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.kramerius.FedoraModels;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl.Mode;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class KrameriusHarvesterNoSorting {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusHarvesterNoSorting.class);

	private static String PID_FIELD = "PID";

	private Long harvestedFrom;

	private Long numFound = 0L;

	private KrameriusHarvesterParams params;

	private HttpClient httpClient;

	private SolrServerFactory solrServerFactory;

	public KrameriusHarvesterNoSorting(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		this.httpClient = httpClient;
		this.solrServerFactory = solrServerFactory;
		this.params = parameters;
		this.harvestedFrom = harvestedFrom;
	}

	public List<String> getUuids(Integer krameriusStart) throws SolrServerException {
		List<String> uuids = new ArrayList<String>();

		SolrDocumentList documents = sendRequest(krameriusStart, PID_FIELD);
		numFound = documents.getNumFound();

		for (SolrDocument document : documents) {
			for (Object pid : document.getFieldValues(PID_FIELD)) {
				uuids.add(pid.toString());
			}
		}
		return uuids;
	}

	public HarvestedRecord downloadRecord(String uuid) throws IOException {

		String recordId;
		HarvestedRecordUniqueId id;
		HarvestedRecord unparsedHr;

		recordId = uuid;
		id = new HarvestedRecordUniqueId(harvestedFrom, recordId);
		unparsedHr = new HarvestedRecord(id);

		String url = createUrl(uuid);
		
		logger.info("Harvesting record from: "+url);
		try (InputStream is = httpClient.executeGet(url)) {
			if (is.markSupported()) {
				is.mark(Integer.MAX_VALUE);
				is.reset();
			}

			unparsedHr.setRawRecord(IOUtils.toByteArray(is));
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			throw new IOException("Harvesting record from: " + url + " caused IOException!");
		}

		// return unparsed(!) HarvestedRecord (most of variables are not set
		// yet)
		return unparsedHr;
	}

	public String createUrl(String uuid) {
		final String baseUrl = params.getUrl();
		final String kramAPIItem = "/item/";
		final String kramAPIStream = "/streams/";
		final String kramStreamType = params.getMetadataStream(); 

		String resultingUrl = baseUrl + kramAPIItem + uuid + kramAPIStream
				+ kramStreamType;
		logger.trace("created URL: {}", resultingUrl);
		return resultingUrl;
	}

	public List<HarvestedRecord> getRecords(List<String> uuids) throws IOException {
		List<HarvestedRecord> records = new ArrayList<HarvestedRecord>();

		for (String s : uuids) {
			HarvestedRecord r = this.downloadRecord(s);			
			if (r !=null) {
				records.add(r);
			} else {
				logger.debug("Skipping HarvestedRecord with uuid: " + s + " [null value returned]");
			}
		}

		return records;
	}

	public SolrDocumentList sendRequest(Integer start, String... fields) throws SolrServerException {
		SolrDocumentList documents = new SolrDocumentList();
		int numProcessed = 0;
		long numFound = 0;

		SolrServerFacade solr = solrServerFactory.create(params.getUrl(), Mode.KRAMERIUS);

		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");	
		
		//works with all possible models in single configuration
		String harvestedModelsStatement = String.join(" OR ",FedoraModels.HARVESTED_MODELS);
		query.add("fq",  harvestedModelsStatement);
		
		if (params.getFrom() != null || params.getUntil() != null) {
			String range = SolrUtils.createDateRange(
					params.getFrom(), params.getUntil());
			query.add("fq", SolrUtils.createFieldQuery("modified_date", range));
		}
		
		logger.info("query: {}", query.getQuery());
		query.setFields(fields);

		if (start != null) {
			query.setStart(start);
		}
		query.setRows((params.getQueryRows() != null) ? params.getQueryRows().intValue() : 10);

		try {
			QueryResponse response = solr.query(query);
			documents = response.getResults();
			numFound = documents.getNumFound();
			numProcessed += response.getResults().size();
		} catch (SolrServerException sse) {
			logger.error(sse.getMessage());
			throw new SolrServerException(String.format("Harvesting list of uuids from Kramerius API: caused SolrServerException for model: %s, url:%s, when processed:%s of %s", params.getModel(), params.getUrl(), numProcessed, numFound));
		}
		return documents;
	}

	public Long getNumFound() {
		return this.numFound;
	}
	
	public static String createQueryString(Map<String, String> query) {
		StringBuilder queryString = new StringBuilder();
		Iterator<String> iterator = query.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = query.get(key);
			queryString.append(key).append(":").append(value);
			if (iterator.hasNext()) {
				queryString.append(" AND ");
			}
		}
		return queryString.toString();
	}
}