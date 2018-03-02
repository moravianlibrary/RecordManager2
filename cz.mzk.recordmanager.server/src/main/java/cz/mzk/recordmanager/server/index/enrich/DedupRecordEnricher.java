package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.model.DedupRecord;

public interface DedupRecordEnricher {

	void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords);

}
