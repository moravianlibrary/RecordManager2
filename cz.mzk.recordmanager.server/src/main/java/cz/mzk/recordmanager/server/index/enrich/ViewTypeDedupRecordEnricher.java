package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewTypeDedupRecordEnricher implements DedupRecordEnricher {

	private static final FieldMerger fieldMerger = new FieldMerger(
			SolrFieldConstants.VIEW_TYPE_TXT_MV);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					List<SolrInputDocument> localRecords) {
		fieldMerger.merge(localRecords, mergedDocument);
	}

}
