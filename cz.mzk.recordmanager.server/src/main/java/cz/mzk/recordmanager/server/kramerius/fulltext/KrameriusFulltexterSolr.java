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
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class KrameriusFulltexterSolr implements KrameriusFulltexter {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltexterSolr.class);

	private static final String FL_FIELDS = String.join(",", UUID_FIELD, FULLTEXT_FIELD, PAGE_NUMBER_FIELD, PAGE_ORDER_FIELD, PID_FIELD);

	private static final int MAX_PAGES = 1000;

	private final SolrServerFacade solr;

	public KrameriusFulltexterSolr(SolrServerFacade solr) {
		super();
		this.solr = solr;
	}

	@Override
	public List<FulltextMonography> getFulltextObjects(String rootUuid)
			throws IOException {
		logger.debug("About to harvest fulltext from Kramerius for {}", rootUuid);
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

			return new ArrayList<FulltextMonography>();
		}
	}

	private List<FulltextMonography> asPages(SolrDocumentList documents) {
		List<FulltextMonography> pages = new ArrayList<FulltextMonography>(documents.size());
		Collections.sort(documents, KrameriusPageComparator.INSTANCE);
		long order = 0L;
		for (SolrDocument document : documents) {
			order++;
			FulltextMonography page = new FulltextMonography();
			
			String uuid = (String) document.getFieldValue(PID_FIELD);
			logger.debug("Harvesting fulltext from Kramerius for page uuid: {}", uuid);
			String fulltext = (String) document.getFieldValue(FULLTEXT_FIELD);
			String pageNum = (String) document.getFieldValue(PAGE_NUMBER_FIELD);
			
			pageNum = pageNum==null ? String.valueOf(order) : pageNum;
			//TODO data sometimes contain garbage values - this should be considered temporary solution
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
	public List<FulltextMonography> getFulltextForRoot(String rootUuid)
			throws IOException {
		logger.debug("About to harvest fulltext from Kramerius for {}", rootUuid);
		SolrQuery query = new SolrQuery();
		
		//<MJ.> here is the change - all pages for given root, should work for serials;-)
		String queryString = SolrUtils.createEscapedFieldQuery("root_pid", rootUuid) + " AND " +
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

			return new ArrayList<FulltextMonography>();
		}
	}

}
