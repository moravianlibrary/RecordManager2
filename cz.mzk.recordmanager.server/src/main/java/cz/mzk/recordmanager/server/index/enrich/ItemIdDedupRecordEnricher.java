package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class ItemIdDedupRecordEnricher implements DedupRecordEnricher {

	private static final FieldMerger FIELD_MERGER = new FieldMerger(
			SolrFieldConstants.ITEM_ID_TXT_MV);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		FIELD_MERGER.mergeAndRemoveFromSources(localRecords, mergedDocument);
	}

}
