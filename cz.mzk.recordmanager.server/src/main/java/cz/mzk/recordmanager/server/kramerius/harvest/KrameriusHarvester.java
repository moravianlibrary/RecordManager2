package cz.mzk.recordmanager.server.kramerius.harvest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class KrameriusHarvester {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusHarvester.class);

	private static String PID_FIELD = "PID";

	private static int MAX_TIME_ALLOWED = 100_000;

	private Long harvestedFrom;

	private KrameriusHarvesterParams params;

	private HttpClient httpClient;

	private SolrServerFactory solrServerFactory;

	public KrameriusHarvester(HttpClient httpClient, SolrServerFactory solrServerFactory,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		this.httpClient = httpClient;
		this.solrServerFactory = solrServerFactory;
		this.params = parameters;
		this.harvestedFrom = harvestedFrom;
	}

	public List<String> getUuids(String nextPid) {
		List<String> uuids = new ArrayList<String>();

		SolrDocumentList documents = sendRequest(nextPid);

		for (SolrDocument document : documents) {
			for (Object pid : document.getFieldValues(PID_FIELD)) {
				uuids.add(pid.toString());
			}
		}
		return uuids;
	}

	public HarvestedRecord downloadRecord(String uuid) {

		String recordId;
		HarvestedRecordUniqueId id;
		HarvestedRecord unparsedHr;

		recordId = uuid;
		id = new HarvestedRecordUniqueId(harvestedFrom, recordId);
		unparsedHr = new HarvestedRecord(id);

		String url = createUrl(uuid);

		try (InputStream is = httpClient.executeGet(url)) {
			if (is.markSupported()) {
				is.mark(Integer.MAX_VALUE);
				is.reset();
			}

			unparsedHr.setRawRecord(IOUtils.toByteArray(is));
			// unparsedHr.setFormat("dublinCore"); // TODO - make it
			// configurable // may be deleted?
		} catch (IOException ioe) {

			// TODO - catch IO Exception properly
		}

		// return unparsed(!) HarvestedRecord (most of variables are not set
		// yet)
		return unparsedHr;
	}

	public String createUrl(String uuid) {
		final String baseUrl = params.getUrl();
		final String kramAPIItem = "/item/";
		final String kramAPIStream = "/streams/";

		final String kramStreamType = params.getMetadataStream(); // /w
																	// KramParams
		// final String kramStreamType = "DC"; // w/ OAIParams

		String resultingUrl = baseUrl + kramAPIItem + uuid + kramAPIStream
				+ kramStreamType;
		logger.trace("created URL: {}", resultingUrl);
		return resultingUrl;
	}

	public List<HarvestedRecord> getRecords(List<String> uuids) {
		List<HarvestedRecord> records = new ArrayList<HarvestedRecord>();

		for (String s : uuids) {
			HarvestedRecord r = this.downloadRecord(s);
			records.add(r);
		}

		return records;
	}

	public SolrDocumentList sendRequest(String nextPid) {
		SolrDocumentList documents = new SolrDocumentList();

		SolrServer solr = solrServerFactory.create(params.getUrl());

		if (solr instanceof HttpSolrServer) {
			((HttpSolrServer) solr).setParser(new XMLResponseParser());
		}
		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		if (nextPid != null) {
			query.add("fq", SolrUtils.createFieldQuery(PID_FIELD, SolrUtils.createRange(nextPid, null)));
		}
		query.add("fq", SolrUtils.createFieldQuery("fedora.model", params.getModel()));
		if (params.getFrom() != null || params.getUntil() != null) {
			String range = SolrUtils.createDateRange(
					params.getFrom(), params.getUntil());
			query.add("fq", SolrUtils.createFieldQuery("modified_date", range));
		}
		logger.info("nextPid: {}", nextPid);
		query.setFields(PID_FIELD);
		query.setSort(PID_FIELD, ORDER.asc);
		query.setRows((params.getQueryRows() != null) ? params.getQueryRows().intValue() : 100);
		query.setTimeAllowed(MAX_TIME_ALLOWED);

		SolrRequest request = new QueryRequest(query);
		request.setPath("/search");
		logger.info("Params: {}", request.getParams());

		try {
			NamedList<Object> req = solr.request(request);
			QueryResponse response = new QueryResponse(req, solr);
			documents = response.getResults();
		} catch (Exception ex) {
			throw new RuntimeException(ex); // FIXME
		}
		return documents;
	}

}
