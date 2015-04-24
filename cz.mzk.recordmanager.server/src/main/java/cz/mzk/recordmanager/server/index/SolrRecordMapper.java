package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface SolrRecordMapper {
	
	public SolrInputDocument map(DedupRecord record, List<HarvestedRecord> records);

	public SolrInputDocument map(HarvestedRecord record);

}
