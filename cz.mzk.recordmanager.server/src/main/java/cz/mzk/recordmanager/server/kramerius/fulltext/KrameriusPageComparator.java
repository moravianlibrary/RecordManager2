package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.Comparator;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.solr.common.SolrDocument;

public enum KrameriusPageComparator implements Comparator<SolrDocument> {

	INSTANCE;

	private static final String DEFAULT_PAGE = "";

	@Override
	public int compare(SolrDocument doc1, SolrDocument doc2) {
		String page1 = getPage(doc1);
		String page2 = getPage(doc2);
		boolean page1IsNumber = NumberUtils.isParsable(page1);
		boolean page2IsNumber = NumberUtils.isParsable(page2);
		if (!page1IsNumber && !page2IsNumber) {
			return page1.compareTo(page2);
		} else if (page1IsNumber && !page2IsNumber) {
			return 1;
		} else if (!page1IsNumber && page2IsNumber) {
			return -1;
		} else {
			int page1Number = Integer.parseInt(page1);
			int page2Number = Integer.parseInt(page2);
			return page1Number - page2Number;
		}
	}

	private String getPage(SolrDocument doc) {
		String page = (String) doc.getFieldValue(KrameriusSolrConstants.PAGE_ORDER_FIELD);
		if (page == null) {
			page = DEFAULT_PAGE;
		}
		return page;
	}

}
