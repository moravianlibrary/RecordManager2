package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class InstitutionDedupRecordEnricher implements DedupRecordEnricher {

	private final FieldMerger copyField = new FieldMerger(
			SolrFieldConstants.LOCAL_INSTITUTION_VIEW_FACET,
			SolrFieldConstants.LOCAL_REGION_INSTITUTION_FACET);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		copyField.merge(localRecords, mergedDocument);
		copyField.renameField(mergedDocument,
				SolrFieldConstants.LOCAL_REGION_INSTITUTION_FACET,
				SolrFieldConstants.REGION_INSTITUTION_FACET);
		copyField.renameField(mergedDocument,
				SolrFieldConstants.LOCAL_INSTITUTION_VIEW_FACET,
				SolrFieldConstants.INSTITUTION_VIEW_FACET);
	}

}
