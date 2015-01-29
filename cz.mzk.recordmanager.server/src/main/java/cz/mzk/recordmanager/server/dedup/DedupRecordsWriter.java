package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.RecordLink;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.RecordLinkDAO;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvester;

public class DedupRecordsWriter implements ItemWriter<HarvestedRecord>  {
	
	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);
	
	@Autowired
	private DedupRecordLocator dedupRecordLocator;
	
	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private RecordLinkDAO recordLinkDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void write(List<? extends HarvestedRecord> records) throws Exception {
		for (HarvestedRecord record : records) {
			logger.debug("About to process record: {}", record);
			DedupRecord deduped = dedupRecordLocator.locate(record);
			if (deduped == null) {
				deduped = new DedupRecord();
				deduped.setIsbn(record.getIsbn());
				deduped.setTitle(record.getTitle());
				dedupRecordDao.persist(deduped);
			}
			updateLink(record, deduped);
		}
	}

	private void updateLink(HarvestedRecord record, DedupRecord deduped) {
		RecordLink currentLink = recordLinkDao.findByHarvestedRecord(record);
		if (currentLink == null || !currentLink.getDedupRecord().equals(deduped)) {
			logger.debug("creating record link between {} and {}", record, deduped);
			if (currentLink != null) {
				recordLinkDao.delete(currentLink);
			}
			RecordLink newLink = new RecordLink(record, deduped);
			recordLinkDao.persist(newLink);
		}
	}

}
