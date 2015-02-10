package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.RecordLinkDAO;

public class SolrRecordProcessor implements ItemProcessor<Long, SolrInputDocument> {
	
	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	@Autowired
	private RecordLinkDAO recordLinkDao;
	
	@Autowired
	private DelegatingSolrRecordMapper mapper;
	
	@Override
	public SolrInputDocument process(Long dedupRecordId) throws Exception {
		DedupRecord dedupRecord = dedupRecordDao.load(dedupRecordId);
		List<HarvestedRecord> records = recordLinkDao.getHarvestedRecords(dedupRecord);
		return mapper.map(dedupRecord, records);
	}

}
