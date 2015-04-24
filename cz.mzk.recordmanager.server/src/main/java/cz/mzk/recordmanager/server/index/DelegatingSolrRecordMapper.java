package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class DelegatingSolrRecordMapper implements SolrRecordMapper {

	private static Logger logger = LoggerFactory.getLogger(DelegatingSolrRecordMapper.class);

	@Autowired
	private SolrRecordMapper solrRecordMapper;

	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (logger.isTraceEnabled()) {
			logger.info("About to map dedupRecord with id = {}", dedupRecord.getId());
		}
		SolrInputDocument result = solrRecordMapper.map(dedupRecord, records);
		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return result;
	}

	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrInputDocument result = solrRecordMapper.map(record);
		return result;
	}

}
