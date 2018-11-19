package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.metadata.view.ViewType;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class ViewTypeDedupRecordEnricher implements DedupRecordEnricher {

	private static final FieldMerger fieldMerger = new FieldMerger(
			SolrFieldConstants.VIEW_TYPE_TXT_MV);

	private static final String MUSICAL_SCORE = SolrUtils.createHierarchicFacetValues(
			HarvestedRecordFormatEnum.MUSICAL_SCORES.name()).get(0);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					List<SolrInputDocument> localRecords) {
		if (localRecords.stream().anyMatch(rec -> rec.containsKey(SolrFieldConstants.VIEW_TYPE_TXT_MV)
				&& rec.getFieldValues(SolrFieldConstants.VIEW_TYPE_TXT_MV).contains(ViewType.IREL.getValue()))
				&& localRecords.stream().anyMatch(rec -> rec.containsKey(SolrFieldConstants.RECORD_FORMAT_DISPLAY)
				&& rec.getFieldValues(SolrFieldConstants.RECORD_FORMAT_DISPLAY).contains(MUSICAL_SCORE))) {
			for (SolrInputDocument local : localRecords) {
				if (local.containsKey(SolrFieldConstants.VIEW_TYPE_TXT_MV)) {
					Collection<Object> views = local.getFieldValues(SolrFieldConstants.VIEW_TYPE_TXT_MV);
					views.remove(ViewType.IREL.getValue());
					local.setField(SolrFieldConstants.VIEW_TYPE_TXT_MV, views);
				}
			}
		}

		fieldMerger.merge(localRecords, mergedDocument);
	}

}
