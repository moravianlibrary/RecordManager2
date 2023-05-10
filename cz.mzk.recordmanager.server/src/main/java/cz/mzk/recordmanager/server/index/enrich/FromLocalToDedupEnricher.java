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
			SolrFieldConstants.CALLNUMBER_SEARCH_TXT_MV,
			SolrFieldConstants.OBALKY_ANNOTATION,
			SolrFieldConstants.GENRE_FACET,
			SolrFieldConstants.CONSPECTUS_FACET,
			SolrFieldConstants.SUBJECT_STR_MV,
			SolrFieldConstants.SUBJECT_FACET,
			SolrFieldConstants.FULLTEXT_ANALYSER,
			SolrFieldConstants.SEMANTIC_ENRICHMENT,
			SolrFieldConstants.AUTO_CONSPECTUS,
			SolrFieldConstants.ZISKEJ_FACET_MV,
			SolrFieldConstants.SCALE_FACET_MV,
			SolrFieldConstants.PUBLISHER_SEARCH_TXT_MV,
			SolrFieldConstants.ZISKEJ_MVS_SIGLAS,
			SolrFieldConstants.ZISKEJ_EDD_SIGLAS
	);

	private final FieldMerger merge = new FieldMerger(
			SolrFieldConstants.MONOGRAPHIC_SERIES_TXT_MV,
			SolrFieldConstants.MONOGRAPHIC_SERIES_DISPLAY_MV,
			SolrFieldConstants.UUID_STR_MV
	);

	private final FieldMerger copySummary = new FieldMerger(
			SolrFieldConstants.SUMMARY_DISPLAY_MV
	);

	private final FieldMerger copyDedup = new FieldMerger();

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		mergeAndRemove.mergeAndRemoveFromSources(localRecords, mergedDocument);
		merge.merge(localRecords, mergedDocument);
		copyDedup.copyField(mergedDocument, SolrFieldConstants.OBALKY_ANNOTATION, SolrFieldConstants.SUMMARY_DISPLAY_MV);
		if (!mergedDocument.containsKey(SolrFieldConstants.SUMMARY_DISPLAY_MV)
				|| mergedDocument.getFieldValues(SolrFieldConstants.SUMMARY_DISPLAY_MV) == null
				|| mergedDocument.getFieldValues(SolrFieldConstants.SUMMARY_DISPLAY_MV).isEmpty()) {
			copySummary.copyFirstFieldFromLocal(localRecords, mergedDocument);
		}
	}
}
