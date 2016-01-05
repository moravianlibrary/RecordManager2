package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class ObalkyKnihTocDedupRecordEnricher implements DedupRecordEnricher {

	private final FieldMerger tocFieldMerger = new FieldMerger(SolrFieldConstants.TOC);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		tocFieldMerger.mergeAndRemoveFromSources(localRecords, mergedDocument);
	}

}
