package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookportDuplicitLocalIdFilterDedupRecordEnricher implements DedupRecordEnricher {


	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		try {
			SolrInputDocument doc = localRecords.get(0);
			if (doc.getFieldValue(SolrFieldConstants.ID_FIELD).toString().startsWith(Constants.PREFIX_BOOKPORT)
					&& localRecords.size() > 1) {
				localRecords.subList(1, localRecords.size()).clear();
				if (mergedDocument.containsKey(SolrFieldConstants.VIEW_TYPE_TXT_MV)) {
					doc.setField(SolrFieldConstants.VIEW_TYPE_TXT_MV,
							mergedDocument.getFieldValues(SolrFieldConstants.VIEW_TYPE_TXT_MV));
				}
			}
		} catch (Exception ignore) {
		}
	}

}
