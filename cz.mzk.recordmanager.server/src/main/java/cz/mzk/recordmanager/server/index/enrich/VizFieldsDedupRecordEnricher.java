package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class VizFieldsDedupRecordEnricher implements DedupRecordEnricher {

	private static final FieldMerger fieldMerger = new FieldMerger(
			SolrFieldConstants.SUBJECT_VIZ_FIELD,
			SolrFieldConstants.GENRE_VIZ_FIELD,
			SolrFieldConstants.CORPORATION_VIZ_FIELD,
			SolrFieldConstants.AUTHOR_VIZ_FIELD);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		fieldMerger.mergeAndRemoveFromSources(localRecords, mergedDocument);
	}

}
