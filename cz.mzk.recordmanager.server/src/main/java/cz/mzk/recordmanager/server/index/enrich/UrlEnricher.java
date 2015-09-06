package cz.mzk.recordmanager.server.index.enrich;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class UrlEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<Object> urls = new HashSet<>();
		localRecords.stream()
			.filter(rec -> rec.getFieldValue(SolrFieldConstants.URL) != null)
			.forEach(rec -> urls.addAll(rec.getFieldValues(SolrFieldConstants.URL)));
		mergedDocument.remove(SolrFieldConstants.URL);
		mergedDocument.addField(SolrFieldConstants.URL, urls);
		
		mergedDocument.remove(SolrFieldConstants.KRAMERIUS_DUMMY_RIGTHS);
		
		localRecords.stream().forEach(doc -> doc.remove(SolrFieldConstants.URL));
	}

}
