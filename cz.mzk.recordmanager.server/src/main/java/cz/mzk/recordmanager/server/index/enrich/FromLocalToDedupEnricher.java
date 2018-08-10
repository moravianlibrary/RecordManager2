package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FromLocalToDedupEnricher implements DedupRecordEnricher {

	private final FieldMerger holdingsFieldMerger = new FieldMerger(
			SolrFieldConstants.AUTHOR_SEARCH_TXT_MV,
			SolrFieldConstants.TITLE_SEARCH_TXT_MV,
			SolrFieldConstants.CALLNUMBER_SEARCH_TXT_MV
	);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		holdingsFieldMerger.mergeAndRemoveFromSources(localRecords, mergedDocument);
	}
}
