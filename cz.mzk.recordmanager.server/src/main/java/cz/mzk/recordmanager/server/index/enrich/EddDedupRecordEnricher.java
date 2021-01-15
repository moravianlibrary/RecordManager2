package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EddDedupRecordEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		boolean result = false;
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.EDD_BOOLEAN)
					&& (Boolean) localRecord.getFieldValue(SolrFieldConstants.EDD_BOOLEAN)) {
				result = true;
				break;
			}
		}
		mergedDocument.addField(SolrFieldConstants.EDD_BOOLEAN, result);
	}

}
