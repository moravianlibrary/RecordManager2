package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FromLocalToDedupEnricher implements DedupRecordEnricher {

	private final FieldMerger mergeAndRemove = new FieldMerger(
			SolrFieldConstants.AUTHOR_SEARCH_TXT_MV,
			SolrFieldConstants.TITLE_SEARCH_TXT_MV,
			SolrFieldConstants.CALLNUMBER_SEARCH_TXT_MV,
			SolrFieldConstants.OBALKY_ANNOTATION
	);

	private final FieldMerger merge = new FieldMerger(
			SolrFieldConstants.MONOGRAPHIC_SERIES_STR_MV,
			SolrFieldConstants.MONOGRAPHIC_SERIES_DISPLAY_MV
	);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		mergeAndRemove.mergeAndRemoveFromSources(localRecords, mergedDocument);
		merge.merge(localRecords, mergedDocument);
	}
}
