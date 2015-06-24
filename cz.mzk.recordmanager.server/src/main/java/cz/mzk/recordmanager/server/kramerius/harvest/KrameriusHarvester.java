package cz.mzk.recordmanager.server.kramerius.harvest;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer.RemoteSolrException;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusHarvester {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusHarvester.class);
	private Long harvestedFrom;
	private Long numFound = 0L;
	private KrameriusHarvesterParams params;
	private HttpClient httpClient;

	public KrameriusHarvester(HttpClient httpClient,
			KrameriusHarvesterParams parameters, Long harvestedFrom) {
		this.httpClient = httpClient;
		this.params = parameters;
		this.harvestedFrom = harvestedFrom;
	}

	public List<String> getUuids(Integer krameriusStart) {
		SolrDocumentList documents;
		List<String> uuids = new ArrayList<String>();

		documents = sendRequest(krameriusStart, "PID");
		numFound = documents.getNumFound();

		for (int i = 0; i < documents.size(); ++i) {

			// library method works with Object

			// asi by slo udelat i s predpokladem, ze v kazdem zaznamu je jen
			// jedno PID..
			for (Object obj : documents.get(i).getFieldValues("PID")) {
				uuids.add(obj.toString());
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
		System.out.println("created URL: " + resultingUrl);
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

	public SolrDocumentList sendRequest(Integer start, String... fields) {
		SolrDocumentList documents = new SolrDocumentList();
		int numProcessed = 0;
		long numFound = 0;

		HttpSolrServer solr = new HttpSolrServer(params.getUrl());

		solr.setParser(new XMLResponseParser());
		SolrQuery squery = new SolrQuery();

		// creates map of SOLR query parameters and formats them into string,
		// which is set as SolrQuery's query.
		Map<String, String> queryMap = new HashMap<String, String>();

		queryMap.put("fedora.model", params.getModel()); // w/ KrameriusParams
		// queryMap.put("fedora.model", "monograph"); // w/ OAIParams

		queryMap.put("modified_date",
				createSolrDateRange(params.getFrom(), params.getUntil()));
		squery.setQuery(createQueryString(queryMap));

		squery.setFields(fields);

		if (start != null) {
			squery.setStart(start);
		}

		// squery.setRows(20); // w/ OAIParams

		// w/ KramParams
		Long queryRows = params.getQueryRows();
		if (queryRows == null) {
			squery.setRows(10);
		} else {
			squery.setRows(queryRows.intValue()); // possible overflow (but
													// using so large value
													// doesn't make sense..)
		}

		SolrRequest request = new QueryRequest(squery);
		request.setPath("/search");

		try {
			QueryResponse rsp = new QueryResponse(solr.request(request), solr);
			documents = rsp.getResults();
			numFound = documents.getNumFound();
			numProcessed += rsp.getResults().size();
			// System.out.println("nalezeny dokumenty, celkem nalezeno:"
			// + numFound + " a zpracovano je :" + numProcessed);

		} catch (RemoteSolrException rse) {
			// TODO
			throw new RuntimeException(rse);
		} catch (SolrServerException se) {
			// TODO
		} catch (IOException e) {
			// TODO
		}

		return documents;
	}

	public String createQueryString(Map<String, String> query) {
		String queryString = "";

		Iterator<String> iterator = query.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = query.get(key);

			queryString += key + ":" + value;
			if (iterator.hasNext()) {
				queryString += " AND ";
			}
		}
		return queryString;
	}

	public String createSolrDateRange(Date from, Date until) {
		String dateRange = "";
		// 'Z' on the end is necessary.. could not find a way how to put one
		// letter timezone and other letters than Z do not seem to work with
		// SOLR via Kramerius API anyway
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		// no from date specified - fall back to time 0
		if (from == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			from = cal.getTime();
		}

		// no until date specified - fall back to current time
		if (until == null) {
			Calendar cal = Calendar.getInstance();
			until = cal.getTime();
		}

		dateRange = String.format("[%s TO %s]", df.format(from),
				df.format(until));

		return dateRange;
	}

	public Long getNumFound() {
		return this.numFound;
	}

}
