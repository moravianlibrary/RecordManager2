package cz.mzk.recordmanager.server.index;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class SolrHarvestedRecordProcessor implements ItemProcessor<HarvestedRecord, SolrInputDocument> {
	
	private static Logger logger = LoggerFactory.getLogger(SolrHarvestedRecordProcessor.class);
	
	@Autowired
	private SolrInputDocumentFactory factory;

	@Override
	public SolrInputDocument process(HarvestedRecord record) throws Exception {
		logger.debug("About to process harvestedRecord with id={}", record.getUniqueId());
		try {
			return factory.create(record);
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing dedup_record with id=%s", record.getUniqueId()), ex);
			return null;
		}
	}

}
