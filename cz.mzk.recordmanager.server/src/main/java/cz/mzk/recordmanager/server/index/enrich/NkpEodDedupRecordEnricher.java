package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.metadata.view.ViewType;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.scripting.marc.function.mzk.MzkStatusFunctions;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NkpEodDedupRecordEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		boolean isDigitized = false;
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.IS_DIGITIZED)
					&& (boolean) localRecord.getFieldValue(SolrFieldConstants.IS_DIGITIZED)) {
				isDigitized = true;
				break;
			}
		}
		if (!isDigitized) {
			return; // no digitized record found
		}
		// is digitized, remove eod value from facet
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.VIEW_TYPE_TXT_MV)
					&& localRecord.getFieldValues(SolrFieldConstants.VIEW_TYPE_TXT_MV).contains(ViewType.NKP.getValue())
					&& localRecord.containsKey(SolrFieldConstants.LOCAL_VIEW_STATUSES_FACET)) {
				// remove EOD status from facet
				localRecord.getFieldValues(SolrFieldConstants.LOCAL_VIEW_STATUSES_FACET).remove(MzkStatusFunctions.EOD_STATUS);
			}
		}
	}

}
