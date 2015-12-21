package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class Field996bDedupRecordEnricher implements DedupRecordEnricher {

	private final FieldMerger holdingsFieldMerger = new FieldMerger(SolrFieldConstants.FIELD_996b);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		holdingsFieldMerger.mergeAndRemoveFromSources(localRecords, mergedDocument);
	}

}
