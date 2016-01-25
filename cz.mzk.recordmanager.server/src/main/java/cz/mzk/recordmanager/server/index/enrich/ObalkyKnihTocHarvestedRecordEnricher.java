package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;
import cz.mzk.recordmanager.server.util.MetadataUtils;

@Component
public class ObalkyKnihTocHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		List<Long> isbns = getIsbns(document);
		List<String> nbns = getNbns(document);
		if (isbns.isEmpty() && nbns.isEmpty()) {
			return;
		}
		List<Long> isbnList = new ArrayList<Long>();
		for (Object isbnAsObject : isbns) {
			Long isbn = MetadataUtils.toISBN13((String) isbnAsObject);
			if (isbn != null) {
				isbnList.add(isbn);
			}
		}
		if (isbnList.isEmpty()) {
			return;
		}
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setIsbns(isbnList);
		for (ObalkyKnihTOC toc : obalkyKnihTOCDao.query(query)) {
			document.addField(SolrFieldConstants.TOC, toc.getToc());
		}
	}

	private List<Long> getIsbns(SolrInputDocument document) {
		Collection<Object> isbns = document.getFieldValues(SolrFieldConstants.ISBN);
		if (isbns == null) {
			return Collections.emptyList();
		}
		List<Long> isbnList = new ArrayList<Long>();
		for (Object isbnAsObject : isbns) {
			Long isbn = MetadataUtils.toISBN13((String) isbnAsObject);
			if (isbn != null) {
				isbnList.add(isbn);
			}
		}
		return isbnList;
	}

	private List<String> getNbns(SolrInputDocument document) {
		Collection<Object> nbns = document.getFieldValues(SolrFieldConstants.NBN);
		if (nbns == null) {
			return Collections.emptyList();
		}
		List<String> nbnList = new ArrayList<String>();
		for (Object isbnAsObject : nbns) {
			String nbn = (String) isbnAsObject;
			if (nbn != null) {
				nbnList.add(nbn);
			}
		}
		return nbnList;
	}

}
