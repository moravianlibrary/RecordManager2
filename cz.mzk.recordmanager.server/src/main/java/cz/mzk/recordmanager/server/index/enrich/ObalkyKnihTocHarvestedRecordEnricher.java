package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.Collection;
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
		Collection<Object> isbns = document.getFieldValues(SolrFieldConstants.ISBN);
		if (isbns == null) {
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

}
