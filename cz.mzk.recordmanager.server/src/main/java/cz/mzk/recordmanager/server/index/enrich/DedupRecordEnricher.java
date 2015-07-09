package cz.mzk.recordmanager.server.index.enrich;

import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.model.DedupRecord;

public interface DedupRecordEnricher {

	public void enrich(DedupRecord record, SolrInputDocument document);

}
