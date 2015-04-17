package cz.mzk.recordmanager.server.index;

import java.util.Collections;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class SolrHarvestedRecordProcessor implements ItemProcessor<HarvestedRecord, SolrInputDocument> {
	
	private static Logger logger = LoggerFactory.getLogger(SolrHarvestedRecordProcessor.class);
	
	@Autowired
	private DelegatingSolrRecordMapper mapper;

	@Override
	public SolrInputDocument process(HarvestedRecord item) throws Exception {
		logger.debug("About to process harvestedRecord with id={}", item.getId());
		try {
			return mapper.map(null, Collections.singletonList(item));
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing dedup_record with id=%s", item.getId()), ex);
			return null;
		}
	}

}
