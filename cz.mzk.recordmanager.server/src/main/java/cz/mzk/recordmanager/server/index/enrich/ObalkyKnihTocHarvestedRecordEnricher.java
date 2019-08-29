package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;

@Component
public class ObalkyKnihTocHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		List<String> isbns = getIsbns(document);
		List<String> nbns = getNbns(document, SolrFieldConstants.NBN);
		List<String> oclcs = getNbns(document, SolrFieldConstants.OCLC_DISPLAY);
		if (isbns.isEmpty() && nbns.isEmpty()) {
			return;
		}
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setEans(isbns);
		query.setNbns(nbns);
		query.setOclcs(oclcs);
		for (ObalkyKnihTOC toc : obalkyKnihTOCDao.query(query)) {
			document.addField(SolrFieldConstants.TOC, toc.getToc());
		}
	}

	private List<String> getIsbns(SolrInputDocument document) {
		Collection<Object> isbns = document.getFieldValues(SolrFieldConstants.ISBN);
		if (isbns == null) {
			return Collections.emptyList();
		}
		List<String> isbnList = new ArrayList<>();
		for (Object isbnAsObject : isbns) {
			String isbn = ISBNUtils.toISBN13String((String) isbnAsObject);
			if (isbn != null) {
				isbnList.add(isbn);
			}
		}
		return isbnList;
	}

	private List<String> getNbns(SolrInputDocument document, String fieldName) {
		Collection<Object> nbns = document.getFieldValues(fieldName);
		if (nbns == null) {
			return Collections.emptyList();
		}
		List<String> nbnList = new ArrayList<>();
		for (Object nbnAsObject : nbns) {
			String nbn = (String) nbnAsObject;
			if (nbn != null) {
				nbnList.add(nbn);
			}
		}
		return nbnList;
	}

}
