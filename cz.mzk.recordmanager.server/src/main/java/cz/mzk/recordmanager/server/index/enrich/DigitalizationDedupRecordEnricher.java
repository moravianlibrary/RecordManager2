package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DigitalizationDedupRecordEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.IS_DIGITIZED)
					&& (boolean) localRecord.getFieldValue(SolrFieldConstants.IS_DIGITIZED)) {
				return; // is digitized
			}
		}
		// not digitized
		List<String> idsForDigitalization = new ArrayList<>();
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.HIDDEN_AVAILABLE_FOR_DIGITALIZATION)
					&& (boolean) localRecord.getFieldValue(SolrFieldConstants.HIDDEN_AVAILABLE_FOR_DIGITALIZATION)) {
				localRecord.setField(SolrFieldConstants.AVAILABLE_FOR_DIGITALIZATION, true);
				idsForDigitalization.add((String) localRecord.getFieldValue(SolrFieldConstants.ID_FIELD));
			}
		}
		mergedDocument.setField(SolrFieldConstants.IDS_FOR_DIGITALIZATION, idsForDigitalization);
	}

}
