package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AutoConspectusDedupRecordEnricher implements DedupRecordEnricher {

	private final FieldMerger holdingsFieldMerger = new FieldMerger(SolrFieldConstants.AUTO_CONSPECTUS_SEARCH);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		holdingsFieldMerger.mergeAndRemoveFromSources(localRecords, mergedDocument);
	}
}
