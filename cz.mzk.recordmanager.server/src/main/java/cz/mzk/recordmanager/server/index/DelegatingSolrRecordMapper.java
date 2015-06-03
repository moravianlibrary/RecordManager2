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
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

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
		SolrInputDocument document = mapper.map(dedupRecord, records);
		document.addField(SolrFieldConstants.ID_FIELD, dedupRecord.getId());
		document.addField(SolrFieldConstants.MERGED_FIELD, 1);
		document.addField(SolrFieldConstants.WEIGHT, records.get(0).getWeight());
		List<String> localIds = new ArrayList<String>();
		for (HarvestedRecord rec : records) {
			localIds.add(getId(rec));
		}
		document.addField(SolrFieldConstants.LOCAL_IDS_FIELD, localIds);
		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return document;
	}

	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrRecordMapper mapper = mapperByFormat.get(record.getFormat());
		SolrInputDocument document = mapper.map(record);
		String id = getId(record);
		document.addField(SolrFieldConstants.ID_FIELD, id);
		document.addField(SolrFieldConstants.INSTITUTION_FIELD, getInstitutionOfRecord(record));
		document.addField(SolrFieldConstants.MERGED_CHILD_FIELD, 1);
		document.addField(SolrFieldConstants.WEIGHT, record.getWeight());
		return document;
	}

	protected SolrRecordMapper getMapper(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		HarvestedRecord leadingRecord = records.get(0);
		SolrRecordMapper mapper = mapperByFormat.get(leadingRecord.getFormat());
		return mapper;
	}

	protected String getInstitutionOfRecord(HarvestedRecord hr) {
		OAIHarvestConfiguration config = hr.getHarvestedFrom();
		if (config != null
				&& config.getLibrary() != null
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	protected String getId(HarvestedRecord record) {
		String prefix = record.getHarvestedFrom().getIdPrefix();
		String id = ((prefix != null) ? prefix + "." : "") + record.getUniqueId().getRecordId();
		return id;
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
