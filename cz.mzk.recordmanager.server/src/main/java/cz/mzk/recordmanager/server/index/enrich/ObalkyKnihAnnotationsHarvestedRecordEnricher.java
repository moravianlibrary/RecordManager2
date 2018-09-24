package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnnotationDAO;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ObalkyKnihAnnotationsHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private ObalkyKnihAnnotationDAO obalkyKnihAnnotationDao;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		List<Long> isbns = getIsbns(document);
		List<String> nbns = getIdentifiers(document, SolrFieldConstants.CNB_ISN);
		List<String> oclcs = getIdentifiers(document, SolrFieldConstants.OCLC_DISPLAY);
		if (isbns.isEmpty() && nbns.isEmpty() && oclcs.isEmpty()) {
			return;
		}
		ObalkyKnihTOCQuery query = new ObalkyKnihTOCQuery();
		query.setIsbns(isbns);
		query.setNbns(nbns);
		query.setOclcs(oclcs);
		for (ObalkyKnihAnnotation annotation : obalkyKnihAnnotationDao.findByIdentifiers(query)) {
			document.addField(SolrFieldConstants.OBALKY_ANNOTATION, annotation.getAnnotation());
		}
	}

	private List<Long> getIsbns(SolrInputDocument document) {
		Collection<Object> isbns = document.getFieldValues(SolrFieldConstants.ISBN);
		if (isbns == null) return Collections.emptyList();
		List<Long> isbnList = new ArrayList<>();
		for (Object isbnAsObject : isbns) {
			Long isbn = ISBNUtils.toISBN13Long((String) isbnAsObject);
			if (isbn != null) isbnList.add(isbn);
		}
		return isbnList;
	}

	private List<String> getIdentifiers(SolrInputDocument document, String fieldName) {
		Collection<Object> ids = document.getFieldValues(fieldName);
		if (ids == null) return Collections.emptyList();
		List<String> resultIds = new ArrayList<>();
		for (Object idAsObject : ids) {
			String id = (String) idAsObject;
			if (id != null) {
				resultIds.add(id);
			}
		}
		return resultIds;
	}

}
