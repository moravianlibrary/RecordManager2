package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.metadata.view.ViewType;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ViewTypeDedupRecordEnricher implements DedupRecordEnricher {

	private static final FieldMerger fieldMerger = new FieldMerger(
			SolrFieldConstants.VIEW_TYPE_TXT_MV);

	private static final Pattern SPLITTER = Pattern.compile("_");

	private static final List<String> MUSICAL_SCORE = SolrUtils.createHierarchicFacetValues(
			HarvestedRecordFormatEnum.MUSICAL_SCORES.name());
	private static final List<String> BLIND_BRAILLE = SolrUtils.createHierarchicFacetValues(
			SPLITTER.split(HarvestedRecordFormatEnum.BLIND_BRAILLE.name()));
	private static final List<String> VISUAL_DOCUMENTS = SolrUtils.createHierarchicFacetValues(
			HarvestedRecordFormatEnum.VISUAL_DOCUMENTS.name());
	private static final List<String> REMOVE_FORMATS = Arrays.asList(
			MUSICAL_SCORE.get(MUSICAL_SCORE.size() - 1),
			BLIND_BRAILLE.get(BLIND_BRAILLE.size() - 1),
			VISUAL_DOCUMENTS.get(VISUAL_DOCUMENTS.size() - 1));

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					List<SolrInputDocument> localRecords) {
		if (localRecords.stream().anyMatch(rec -> rec.containsKey(SolrFieldConstants.VIEW_TYPE_TXT_MV)
				&& rec.getFieldValues(SolrFieldConstants.VIEW_TYPE_TXT_MV).contains(ViewType.IREL.getValue()))
				&& localRecords.stream().anyMatch(rec -> rec.containsKey(SolrFieldConstants.RECORD_FORMAT_DISPLAY)
				&& !Collections.disjoint(rec.getFieldValues(SolrFieldConstants.RECORD_FORMAT_DISPLAY), REMOVE_FORMATS))) {
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
