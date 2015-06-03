package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class DelegatingSolrRecordMapper implements SolrRecordMapper, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(DelegatingSolrRecordMapper.class);

	@Autowired
	private List<SolrRecordMapper> mappers;

	private Map<String, SolrRecordMapper> mapperByFormat = new HashMap<>();

	@Override
	public List<String> getSupportedFormats() {
		return new ArrayList<>(mapperByFormat.keySet());
	}

	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (logger.isTraceEnabled()) {
			logger.info("About to map dedupRecord with id = {}", dedupRecord.getId());
		}
		SolrRecordMapper mapper = getMapper(dedupRecord, records);
		SolrInputDocument result = mapper.map(dedupRecord, records);
		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return result;
	}

	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrRecordMapper mapper = mapperByFormat.get(record.getFormat());
		SolrInputDocument result = mapper.map(record);
		return result;
	}

	protected SolrRecordMapper getMapper(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		HarvestedRecord leadingRecord = records.get(0);
		SolrRecordMapper mapper = mapperByFormat.get(leadingRecord.getFormat());
		return mapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (SolrRecordMapper mapper : mappers) {
			for (String format : mapper.getSupportedFormats()) {
				mapperByFormat.put(format, mapper);
			}
		}
	}

}
