package cz.mzk.recordmanager.server.index;

import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class SolrHarvestedRecordProcessor implements ItemProcessor<HarvestedRecord, List<SolrInputDocument>> {

	private static Logger logger = LoggerFactory.getLogger(SolrHarvestedRecordProcessor.class);

	@Autowired
	private SolrInputDocumentFactory factory;

	@Override
	public List<SolrInputDocument> process(HarvestedRecord record) throws Exception {
		logger.debug("About to process harvested record with id={}", record.getId());
		try {
			SolrInputDocument result = factory.create(record);
			return Collections.singletonList(result);
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing harvested record with id=%s", record.getId()), ex);
			return null;
		}
	}

}
