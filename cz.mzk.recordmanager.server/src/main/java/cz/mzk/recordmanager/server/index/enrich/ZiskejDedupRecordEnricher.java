package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZiskejDedupRecordEnricher implements DedupRecordEnricher {



	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		boolean result = false;
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.ZISKEJ_BOOLEAN)
					&& (Boolean) localRecord.getFieldValue(SolrFieldConstants.ZISKEJ_BOOLEAN)) {
				result = true;
				break;
			}
		}
		mergedDocument.addField(SolrFieldConstants.ZISKEJ_BOOLEAN, result);
	}

}
