package cz.mzk.recordmanager.server.index.enrich;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class IdentifiersDedupRecordEnricher implements DedupRecordEnricher {

	private final List<FieldMerger> holdingsFieldMerger = Arrays.asList(
			new FieldMerger(SolrFieldConstants.ISBN),
			new FieldMerger(SolrFieldConstants.ISSN),
			new FieldMerger(SolrFieldConstants.ISMN_ISN),
			new FieldMerger(SolrFieldConstants.EAN_ISN),
			new FieldMerger(SolrFieldConstants.CNB_ISN)
	);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		holdingsFieldMerger.forEach(merger -> merger.mergeAndRemoveFromSources(localRecords, mergedDocument));
	}
}
