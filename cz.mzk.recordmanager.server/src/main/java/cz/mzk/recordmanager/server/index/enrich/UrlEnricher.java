package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class UrlEnricher implements DedupRecordEnricher {

	private final FieldMerger urlFieldMerger = new FieldMerger(SolrFieldConstants.URL);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		urlFieldMerger.mergeAndRemoveFromSources(localRecords, mergedDocument);
		mergedDocument.remove(SolrFieldConstants.KRAMERIUS_DUMMY_RIGTHS);
	}

}
