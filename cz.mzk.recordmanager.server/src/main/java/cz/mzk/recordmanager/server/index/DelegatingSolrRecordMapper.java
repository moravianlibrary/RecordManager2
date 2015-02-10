package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class DelegatingSolrRecordMapper implements SolrRecordMapper {

	@Autowired
	private SolrRecordMapper solrRecordMapper;
	
	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		return solrRecordMapper.map(dedupRecord, records);
	}

}
