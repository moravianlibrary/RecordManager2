package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import static cz.mzk.recordmanager.server.kramerius.fulltext.KrameriusSolrConstants.*;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class KrameriusFulltexterSolr implements KrameriusFulltexter {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltexterSolr.class);

	private static final String FL_FIELDS = String.join(",", UUID_FIELD, FULLTEXT_FIELD, PAGE_NUMBER_FIELD, PAGE_ORDER_FIELD, PID_FIELD);

	private static final int MAX_PAGES = 1000;  //number of pages requested in single SOLR query
	
	private static final int PAGE_LIMIT = 50000; //maximal number of downloaded pages for 1 document

	private final SolrServerFacade solr;

	public KrameriusFulltexterSolr(SolrServerFacade solr) {
		super();
		this.solr = solr;
	}

	@Override
	public List<FulltextKramerius> getFulltextObjects(String rootUuid)
			throws IOException {

	/*	logger.debug("About to harvest fulltext from Kramerius for {}", rootUuid);
		SolrQuery query = new SolrQuery();
		String queryString = SolrUtils.createEscapedFieldQuery(PARENT_PID_FIELD, rootUuid) + " AND " +
				SolrUtils.createEscapedFieldQuery(FEDORA_MODEL_FIELD, FEDORA_MODEL_PAGE);
		query.setQuery(queryString);
		query.set("fl", FL_FIELDS);
		query.setRows(MAX_PAGES);
		SolrRequest request = new QueryRequest(query);
		request.setPath("/solr");
		try {
			QueryResponse response = solr.query(request);
			SolrDocumentList documents = response.getResults();
			return asPages(documents);
		} catch (Exception ex) {
			logger.error("Harvesting of fulltext for uuid: {} FAILED", rootUuid);
			logger.error(ex.getMessage());

			return new ArrayList<FulltextKramerius>();
		}*/
		
		int start = 0;
		Long numFound;
		boolean finished=false;
		List<FulltextKramerius> result = new ArrayList<FulltextKramerius>();
		
		while (!finished) {
			
			logger.debug("Downloading fulltext for pages {} to {}", start, start+MAX_PAGES);
			SolrQuery query = new SolrQuery();
			
			String queryString = SolrUtils.createEscapedFieldQuery(PARENT_PID_FIELD, rootUuid) + " AND " +
					SolrUtils.createEscapedFieldQuery(FEDORA_MODEL_FIELD, FEDORA_MODEL_PAGE);
			query.setQuery(queryString);
			query.set("fl", FL_FIELDS);
			query.setRows(MAX_PAGES);
			query.setStart(start);
			SolrRequest request = new QueryRequest(query);
			request.setPath("/solr");
			
			try {
				QueryResponse response = solr.query(request);
				SolrDocumentList documents = response.getResults();
				numFound = documents.getNumFound();
				
				result.addAll(asPages(documents));
				//return asPages(documents);
			} catch (Exception ex) {
				logger.error("Harvesting of fulltext for uuid: {} FAILED", rootUuid);
				logger.error(ex.getMessage());
		
				return result;
			}
			
			start += MAX_PAGES;
			
			if (start >= PAGE_LIMIT) {
				logger.error("Harvesting of fulltext for uuid: {} REACHED LIMIT {} for number of pages for one record", rootUuid, PAGE_LIMIT);
				finished = true;
			}
			
			if (start > numFound) {
				finished = true;
			}
		}
		return result;
		
		
	}

	private List<FulltextKramerius> asPages(SolrDocumentList documents) {
		List<FulltextKramerius> pages = new ArrayList<FulltextKramerius>(documents.size());
		Collections.sort(documents, KrameriusPageComparator.INSTANCE);
		long order = 0L;
		for (SolrDocument document : documents) {
			order++;
			FulltextKramerius page = new FulltextKramerius();
			
			String uuid = (String) document.getFieldValue(PID_FIELD);
			logger.debug("Harvesting fulltext from Kramerius for page uuid: {}", uuid);
			String fulltext = (String) document.getFieldValue(FULLTEXT_FIELD);
			String pageNum = (String) document.getFieldValue(PAGE_NUMBER_FIELD);
			
			pageNum = pageNum==null ? String.valueOf(order) : pageNum;
			//TODO data sometimes contain garbage values - this should be considered fallback solution
			pageNum = pageNum.length() > 50 ? pageNum.substring(0, 50) : pageNum; 
			
			page.setUuidPage(uuid);
			if (fulltext != null) {
				page.setFulltext(fulltext.getBytes(Charsets.UTF_8));
			}
			page.setOrder(order);
			page.setPage(pageNum);
			pages.add(page);
		}
		return pages;
	}
	
	@Override
	public List<FulltextKramerius> getFulltextForRoot(String rootUuid)
			throws IOException {
		logger.debug("About to harvest fulltext from Kramerius for {}", rootUuid);
		
		int start = 0;
		Long numFound;
		boolean finished=false;
		List<FulltextKramerius> result = new ArrayList<FulltextKramerius>();
		
		while (!finished) {
			
			logger.debug("Downloading fulltext for pages {} to {}", start, start+MAX_PAGES);
			SolrQuery query = new SolrQuery();
			
			//<MJ.> here is the change - all pages for given root, should work for serials;-)
			String queryString = SolrUtils.createEscapedFieldQuery(ROOT_PID_FIELD, rootUuid) + " AND " +
					SolrUtils.createEscapedFieldQuery(FEDORA_MODEL_FIELD, FEDORA_MODEL_PAGE);
			query.setQuery(queryString);
			query.set("fl", FL_FIELDS);
			query.setRows(MAX_PAGES);
			query.setStart(start);
			SolrRequest request = new QueryRequest(query);
			request.setPath("/solr");
			
			try {
				QueryResponse response = solr.query(request);
				SolrDocumentList documents = response.getResults();
				numFound = documents.getNumFound();
				
				result.addAll(asPages(documents));
				//return asPages(documents);
			} catch (Exception ex) {
				logger.error("Harvesting of fulltext for uuid: {} FAILED", rootUuid);
				logger.error(ex.getMessage());
		
				return result;
			}
			
			start += MAX_PAGES;
			
			if (start >= PAGE_LIMIT) {
				logger.error("Harvesting of fulltext for uuid: {} REACHED LIMIT {} for number of pages for one record", rootUuid, PAGE_LIMIT);
				finished = true;
			}
			
			if (start > numFound) {
				finished = true;
			}
		}
		return result;
	}

}
