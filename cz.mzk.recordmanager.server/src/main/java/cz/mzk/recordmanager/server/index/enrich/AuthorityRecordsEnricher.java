package cz.mzk.recordmanager.server.index.enrich;

import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class AuthorityRecordsEnricher implements DedupRecordEnricher{

	private final FieldMerger moveField = new FieldMerger(
			SolrFieldConstants.SUBJECT_KEYWORDS_SEARCH,
			SolrFieldConstants.PEOPLE_SEARCH,
			SolrFieldConstants.SUBJECT_FACET,
			SolrFieldConstants.AUTHOR_CORPORATION_SEARCH
			);
	
	private final FieldMerger copyFields = new FieldMerger(SolrFieldConstants.ID_AUTHORITY);
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		Collection<Object> institutions = mergedDocument.getFieldValues(SolrFieldConstants.INSTITUTION_FIELD);

		if(!institutions.containsAll(SolrUtils.createHierarchicFacetValues("Others", "AUTHORITY"))) return;
		
		// is authority record
		
		mergedDocument.removeField(SolrFieldConstants.AUTHOR_FIELD);
		mergedDocument.removeField(SolrFieldConstants.AUTHOR_FIND);
		mergedDocument.removeField(SolrFieldConstants.AUTHOR_SORT_STR);
		mergedDocument.removeField(SolrFieldConstants.AUTHOR_FACET);
		
		moveField.mergeAndRemoveFromSources(localRecords, mergedDocument);
		copyFields.merge(localRecords, mergedDocument);
		
	}

}
