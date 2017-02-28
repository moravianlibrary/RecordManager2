package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface SolrInputDocumentFactory {

	public SolrInputDocument create(HarvestedRecord record);

	public List<SolrInputDocument> create(DedupRecord dedupRecord, List<HarvestedRecord> records);

	public SolrInputDocument create(AdresarKnihoven record);
	
}
