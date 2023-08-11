package cz.mzk.recordmanager.server.kramerius.fulltext;

import com.google.common.base.Charsets;
import cz.mzk.recordmanager.server.kramerius.ApiMappingEnum;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static cz.mzk.recordmanager.server.kramerius.ApiMappingEnum.*;
import static cz.mzk.recordmanager.server.kramerius.fulltext.KrameriusSolrConstants.*;

public class KrameriusFulltexterSolr implements KrameriusFulltexter {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltexterSolr.class);

	private static String FL_FIELDS = null;

	private static final int MAX_PAGES = 1000;  // number of pages requested in single SOLR query

	private static final int PAGE_LIMIT = 50000; // maximal number of downloaded pages for 1 document

	private static final Pattern POLICY_PUBLIC = Pattern.compile("public");

	private final SolrServerFacade solr;

	private final KrameriusHarvesterParams params;

	public KrameriusFulltexterSolr(SolrServerFacade solr) {
		super();
		this.solr = solr;
		this.params = null;
	}

	public KrameriusFulltexterSolr(SolrServerFacade solr, KrameriusHarvesterParams params) {
		super();
		this.solr = solr;
		this.params = params;
		FL_FIELDS = String.join(",", UUID_FIELD, FULLTEXT_FIELD,
				params.getApiMappingValue(ApiMappingEnum.PAGE_NUMBER),
				params.getApiMappingValue(ApiMappingEnum.PAGE_ORDER),
				params.getApiMappingValue(ApiMappingEnum.PID),
				params.getApiMappingValue(ApiMappingEnum.ACCESSIBILITY));
	}

	@Override
	public List<FulltextKramerius> getFulltextObjects(String rootUuid) throws IOException, SolrServerException {
		return getFulltextObjects(params.getApiMappingValue(ApiMappingEnum.PARENT_PID), rootUuid);
	}

	@Override
	public List<FulltextKramerius> getFulltextForRoot(String rootUuid) throws IOException, SolrServerException {
		return getFulltextObjects(params.getApiMappingValue(ApiMappingEnum.ROOT_PID), rootUuid);
	}

	protected List<FulltextKramerius> getFulltextObjects(String field, String rootUuid)
			throws IOException, SolrServerException {
		int start = 0;
		long numFound = 0;
		boolean finished = false;
		List<FulltextKramerius> result = new ArrayList<FulltextKramerius>();

		while (!finished) {

			logger.debug("Downloading fulltext for pages {} to {}", start, start + MAX_PAGES);
			SolrQuery query = new SolrQuery();

			String queryString = SolrUtils.createEscapedFieldQuery(field, rootUuid) + " AND " +
					SolrUtils.createEscapedFieldQuery(params.getApiMappingValue(MODEL), FEDORA_MODEL_PAGE);
			query.setQuery(queryString);
			query.set("fl", FL_FIELDS);
			query.setRows(MAX_PAGES);
			query.setStart(start);
			try {
				QueryResponse response = solr.query(query);
				SolrDocumentList documents = response.getResults();
				numFound = documents.getNumFound();

				result.addAll(asPages(documents));
			} catch (Exception ex) {
				logger.error("Harvesting of fulltext for uuid: {} FAILED", rootUuid);
				logger.error(ex.getMessage());

				throw ex;
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

			String uuid = (String) document.getFieldValue(params.getApiMappingValue(PID));
			logger.debug("Harvesting fulltext from Kramerius for page uuid: {}", uuid);
			String fulltext = (String) document.getFieldValue(FULLTEXT_FIELD);
			String pageNum = (String) document.getFieldValue(params.getApiMappingValue(PAGE_NUMBER));
			String policy = (String) document.getFieldValue(params.getApiMappingValue(ACCESSIBILITY));
			if (policy == null) policy = "private";
			pageNum = (pageNum == null) ? String.valueOf(order) : pageNum;
			//TODO data sometimes contain garbage values - this should be considered fallback solution
			pageNum = pageNum.length() > 50 ? pageNum.substring(0, 50) : pageNum;

			page.setUuidPage(uuid);
			if (fulltext != null) {
				page.setFulltext(fulltext.getBytes(Charsets.UTF_8));
			}
			page.setOrder(order);
			page.setPage(pageNum);
			page.setPrivate(!POLICY_PUBLIC.matcher(policy).matches());
			pages.add(page);
		}
		return pages;
	}

}
