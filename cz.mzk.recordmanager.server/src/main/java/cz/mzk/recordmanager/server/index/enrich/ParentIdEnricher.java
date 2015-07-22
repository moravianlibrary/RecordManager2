package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class ParentIdEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {

		Long parentId = record.getId();

		for (SolrInputDocument currentDoc: localRecords) {
			currentDoc.addField(SolrFieldConstants.PARENT_ID, parentId.toString());
		}

	}
}
