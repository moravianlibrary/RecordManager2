package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class HoldingsDedupRecordEnricher implements DedupRecordEnricher {

	@Override
	@SuppressWarnings("unchecked")
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		List<String> allHoldings996 = new ArrayList<>();
		localRecords.stream() //
			.map(doc -> doc.getField(SolrFieldConstants.HOLDINGS_996_FIELD)) //
			.filter(field -> field != null && field.getValue() != null) //
			.forEach(field -> allHoldings996.addAll((List<String>) field.getValue()));
		mergedDocument.remove(SolrFieldConstants.HOLDINGS_996_FIELD);
		mergedDocument.addField(SolrFieldConstants.HOLDINGS_996_FIELD, allHoldings996);
	}

}
