package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class DedupRecordsWriter implements ItemWriter<HarvestedRecord>  {

	private static final String UPDATE_QUERY = "UPDATE harvested_record SET dedup_record_id = ? WHERE id = ?";
	
	@Autowired
	private DedupRecordLocator dedupRecordLocator;
	
	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void write(List<? extends HarvestedRecord> records) throws Exception {
		for (HarvestedRecord record : records) {
			DedupRecord deduped = dedupRecordLocator.locate(record);
			if (deduped == null) {
				deduped = new DedupRecord();
				deduped.setIsbn(record.getIsbn());
				deduped.setTitle(record.getTitle());
				dedupRecordDao.persist(deduped);
				dedupRecordDao.flush();
			}
			if (!deduped.equals(record.getDedupRecord())) {
				jdbcTemplate.update(UPDATE_QUERY, deduped.getId(), record.getId());
			}
		}
	}

}
