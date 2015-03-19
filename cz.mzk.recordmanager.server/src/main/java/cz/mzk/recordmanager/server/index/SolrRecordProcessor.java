package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.RecordLinkDAO;

public class SolrRecordProcessor implements ItemProcessor<Long, SolrInputDocument> {
	
	private static Logger logger = LoggerFactory.getLogger(SolrRecordProcessor.class);
	
	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	@Autowired
	private RecordLinkDAO recordLinkDao;
	
	@Autowired
	private DelegatingSolrRecordMapper mapper;
	
	@Override
	public SolrInputDocument process(Long dedupRecordId) throws Exception {
		logger.debug("About to process dedup_record with id={}", dedupRecordId);
		DedupRecord dedupRecord = dedupRecordDao.get(dedupRecordId);
		List<HarvestedRecord> records = recordLinkDao.getHarvestedRecords(dedupRecord);
		if (records.isEmpty()) {
			throw new IllegalArgumentException("records is empty");
		}
		try {
			return mapper.map(dedupRecord, records);
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing dedup_record with id=%s", dedupRecordId), ex);
			return null;
		}
	}

}
