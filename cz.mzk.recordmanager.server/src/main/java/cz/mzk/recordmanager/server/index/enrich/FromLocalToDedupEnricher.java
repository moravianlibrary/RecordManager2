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
			SolrFieldConstants.OBALKY_ANNOTATION,
			SolrFieldConstants.STATUSES_FACET,
			SolrFieldConstants.FULLTEXT_ANALYSER,
			SolrFieldConstants.SEMANTIC_ENRICHMENT,
			SolrFieldConstants.AUTO_CONSPECTUS
	);

	private final FieldMerger merge = new FieldMerger(
			SolrFieldConstants.MONOGRAPHIC_SERIES_TXT_MV,
			SolrFieldConstants.MONOGRAPHIC_SERIES_DISPLAY_MV
	);

	private final FieldMerger copyDedup = new FieldMerger();

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		mergeAndRemove.mergeAndRemoveFromSources(localRecords, mergedDocument);
		merge.merge(localRecords, mergedDocument);
		copyDedup.copyField(mergedDocument, SolrFieldConstants.OBALKY_ANNOTATION, SolrFieldConstants.SUMMARY_DISPLAY_MV);
	}
}
