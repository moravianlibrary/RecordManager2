package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class SolrRecordProcessor implements ItemProcessor<DedupRecord, List<SolrInputDocument>> {
	
	private static Logger logger = LoggerFactory.getLogger(SolrRecordProcessor.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private SolrInputDocumentFactory factory;

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public List<SolrInputDocument> process(DedupRecord dedupRecord) throws Exception {
		logger.debug("About to process dedup_record with id={}", dedupRecord.getId());
		try {
			List<HarvestedRecord> records = harvestedRecordDao.getByDedupRecord(dedupRecord);
			if (records.isEmpty()) {
				throw new IllegalArgumentException("records is empty");
			}
			// don't index deleted records
			List<SolrInputDocument> result = factory.create(
					dedupRecord,
					records.stream()
						.filter(p -> p.getDeleted() == null)
						.collect(Collectors.toCollection(ArrayList::new))
					);
			logger.debug("Processing of dedup_record with id={} finished", dedupRecord.getId());
			return result;
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing dedup_record with id=%s", dedupRecord.getId()), ex);
			return null;
		}
	}

}
