package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LongLatDedupEnricher implements DedupRecordEnricher {

	private final FieldMerger merge = new FieldMerger(
			SolrFieldConstants.LONG_LAT_DISPLAY_MV
	);

	private final FieldMerger fieldMerger = new FieldMerger();

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		merge.merge(localRecords, mergedDocument);
		fieldMerger.renameField(mergedDocument, SolrFieldConstants.LONG_LAT_DISPLAY_MV, SolrFieldConstants.LONG_LAT);
		if (mergedDocument.containsKey(SolrFieldConstants.LONG_LAT)
				&& mergedDocument.getFieldValues(SolrFieldConstants.LONG_LAT) != null
				&& !mergedDocument.getFieldValues(SolrFieldConstants.LONG_LAT).isEmpty()) {
			mergedDocument.setField(SolrFieldConstants.LONG_LAT,
					new ArrayList<>(mergedDocument.getFieldValues(SolrFieldConstants.LONG_LAT)).get(0));
		}
		fieldMerger.copyField(mergedDocument, SolrFieldConstants.LONG_LAT, SolrFieldConstants.LONG_LAT_STR);
	}

}
