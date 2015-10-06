package cz.mzk.recordmanager.server.index.enrich;

import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface HarvestedRecordEnricher {

	public void enrich(HarvestedRecord record, SolrInputDocument document);

}
