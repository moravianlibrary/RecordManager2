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
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvester;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterParams;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusHarvester {

	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);
	private static Long harvestedFrom;
	private static Long numFound = 0L;
	private OAIHarvesterParams params;
	private HttpClient httpClient;
	
	
	
	public KrameriusHarvester(HttpClient httpClient, OAIHarvesterParams parameters, Long harvestedFrom) {
		this.httpClient = httpClient;
		this.params= parameters;
		this.harvestedFrom = harvestedFrom;
	}

	public List<String> getUuids(Integer krameriusStart) {
		SolrDocumentList documents;
		List<String> uuids = new ArrayList<String>();
	
		
		documents = sendRequest(krameriusStart, "PID");
		numFound = documents.getNumFound();
		
		for (int i = 0; i < documents.size(); ++i) {

			// pracuje se s obecnym objektem - asi se neda uplne rict co SOLR
			// vrati
			
			// asi by slo udelat i s predpokladem, ze v kazdem zaznamu je jen jedno PID..
			for (Object obj: documents.get(i).getFieldValues("PID")) {
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
		
		//vytahne data z parametru
		//vytvori URL
		String url = createUrl(uuid);
		//posle pozadavek pres klienta
		if (httpClient == null) {
			System.out.println("httpClient je TED null");
			} else {
				System.out.println("httpClient TED neni null");
			}
		
		try (InputStream is = httpClient.executeGet(url)) {
			if (is == null) {
				System.out.println("IS je TED null");
				} else {
					System.out.println("IS TED neni null");
				}
			
			if (is.markSupported()) {
				is.mark(Integer.MAX_VALUE);
				is.reset();
							
			}
			
			unparsedHr.setRawRecord(IOUtils.toByteArray(is));
			unparsedHr.setFormat("dublinCore"); //TODO - make it configurable
		} catch (IOException ioe) {
			
			//TODO - catch IO Exception properly
		}
		

			// return unparsed(!) HarvestedRecord (most of variables are not set yet)
			return unparsedHr;
	}
	
	public String createUrl(String uuid) {
		final String baseUrl = params.getUrl();
		final String kramAPIItem = "/item/";
		final String kramAPIStream = "/streams/";
		
//		final String kramStreamType = params.getMetadataStreamType();  	// /w KramParams
		final String kramStreamType = "DC"; 							// w/ OAIParams
		
	    String resultingUrl = baseUrl + kramAPIItem + uuid + kramAPIStream + kramStreamType;
	    System.out.println("created URL: " + resultingUrl);
	    return resultingUrl;
	}
	public List<HarvestedRecord> getRecords(List<String> uuids) {
		List<HarvestedRecord> records = new ArrayList<HarvestedRecord>();
		
		for (String s: uuids) {
			HarvestedRecord r = this.downloadRecord(s);
			records.add(r);
		}
		
		
		return records;
	}

	//sendRequest
	// dostane parametry
	// vrati SolrResult	
	public SolrDocumentList sendRequest(Integer start, String...fields) {
		SolrDocumentList documents = new SolrDocumentList();
		int numProcessed = 0;
		long numFound = 0;
		
		HttpSolrServer solr = new HttpSolrServer(params.getUrl());
		
		solr.setParser(new XMLResponseParser()); 
		SolrQuery squery = new SolrQuery();
		
		//creates map of SOLR query parameters and formats them into string, which is set as SolrQuery's query.
		Map<String,String> queryMap = new HashMap<String,String>();
//		System.out.println("nastavuji mapu parametru pro query...");

//		queryMap.put("fedora.model", params.getModel()); // w/ KrameriusParams
		queryMap.put("fedora.model", "monograph"); // w/ OAIParams
		
		queryMap.put("modified_date", createSolrDateRange(params.getFrom(), params.getUntil()));
//		System.out.println("predavam mapu parametru query...");
		squery.setQuery(createQueryString(queryMap));
		
		squery.setFields(fields);
		
		if (start != null) {
			squery.setStart(start);
		}
// w/ KramParams
//		if (params.getQueryRows() != null) {
//			squery.setRows(params.getQueryRows());
//		}

		squery.setRows(20);  // w/ OAIParams
		
		SolrRequest request = new QueryRequest(squery);
		request.setPath("/search");
//		System.out.println("nastavena cesta: "+ request.getPath());
		
		try {
			QueryResponse rsp = new QueryResponse(solr.request(request), solr);
			documents = rsp.getResults();
			numFound = documents.getNumFound();
			numProcessed += rsp.getResults().size();  
			System.out.println("nalezeny dokumenty, celkem nalezeno:" + numFound + " a zpracovano je :" + numProcessed);
			//build more requests, if numfound > processed rows
//			while (numProcessed<numFound) {
//				squery.setStart(numProcessed);
//				request = new QueryRequest(squery);
//				request.setPath("/search");
				
//				rsp = new QueryResponse(solr.request(request), solr);
//				numProcessed += rsp.getResults().size();  
//				documents.addAll(rsp.getResults());
//				System.out.println("zpracovano zaznamu: " + numProcessed + " z celkovych: "+ numFound);
//			}
		} catch (RemoteSolrException rse) {
			//TODO
			System.out.println("chycena vyjimka");
			System.out.println("server: " + solr.getBaseURL());
			System.out.println("request params: " + request.getParams().toString());

			System.out.println("Solr Server Exception: "+ rse);	
			throw new RuntimeException(rse);
			
		} catch (SolrServerException se) {
			//TODO
			System.out.println("Solr Server Exception: "+ se.getMessage());		
			
		} catch (IOException e) {
			//TODO
			System.out.println("IO Exception: " + e.getMessage());
		}
		
		
		System.out.println("vraci se dokumenty..");
		return documents;
	}
	
	public String createQueryString(Map<String,String> query) {
		String queryString ="";
		
		Iterator<String> iterator = query.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
		    String value = query.get(key);
		    
		    queryString +=  key+":"+value;
		    if (iterator.hasNext()) {
		    queryString += " AND ";	
		    }
		 }
//		System.out.println("queryString: "+queryString);

		return queryString;
	}
	
	public String createSolrDateRange(Date from, Date until){		
		String dateRange ="";
		System.out.println("vytvarim date range...");
		// 'Z' on the end is necessary.. could not find a way how to put one letter timezone and other letters than Z do not seem to work with SOLR via Kramerius API anyway
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		//no from date specified - fall back to time 0
		if (from == null) {
			System.out.println("from je null!");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			from = cal.getTime();
		}
		
		//no until date specified - fall back to current time
		if (until == null) {
			System.out.println("until je null!");
			Calendar cal = Calendar.getInstance();
			until = cal.getTime();
		}	
		
		dateRange = String.format("[%s TO %s]", df.format(from), df.format(until));
		System.out.println("vysledna dateRange: "+dateRange);
		return dateRange;
	}
	
	public Long getNumFound() {
		return this.numFound;
	}
	
}
