package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class SolrHarvestedRecordProcessor implements ItemProcessor<HarvestedRecord, List<SolrInputDocument>> {
	
	private static Logger logger = LoggerFactory.getLogger(SolrHarvestedRecordProcessor.class);

	@Autowired
	private SolrInputDocumentFactory factory;

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public List<SolrInputDocument> process(HarvestedRecord record) throws Exception {
		logger.debug("About to process harvestedRecord with id={}", record.getUniqueId());
		try {
			List<SolrInputDocument> result = new ArrayList<>();
			//filter deleted records
			if (record.getDeleted() == null) { 
				result.add(factory.create(record));
			}
			return result;
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing dedup_record with id=%s", record.getUniqueId()), ex);
			return null;
		}
	}

}
