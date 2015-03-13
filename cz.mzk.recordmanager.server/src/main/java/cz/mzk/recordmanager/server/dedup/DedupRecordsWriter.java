package cz.mzk.recordmanager.server.dedup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class DedupRecordsWriter implements ItemWriter<HarvestedRecord>  {
	
	private static Logger logger = LoggerFactory.getLogger(DedupRecordsWriter.class);
	
	@Autowired
	private HarvestedRecordDedupMatcher recordMatcher;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	private static final String FIND_CANDIDATES_QUERY = "SELECT id,isbn,title,publication_year,physical_format "
			+ "FROM harvested_record hr "
			+ "WHERE hr.id != ? and (hr.isbn = ? OR hr.title LIKE ?) "
			+ "ORDER BY hr.id";
	
	private static final String FIND_EXISTING_DEDUP_RECORD_QUERY = "SELECT * "
			+ "FROM dedup_record dr "
			+ "WHERE id IN (SELECT dedup_record_id FROM record_link rl WHERE rl.harvested_record_id = ?)"
			+ "LIMIT 1";
	
	private static final String CALL_UPDATE_PROCEDURE = "SELECT * FROM update_record_links(?,?)";
	
	/**
	 * logging properties
	 */
	private static final int LOG_PERIOD = 1000;
	private int totalCount  = 0;
	private int locateCount = 0;
	private long startTime = 0L;

	private static enum DedupRecordRowMapper implements RowMapper<DedupRecord> {

		INSTANCE;

		@Override
		public DedupRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
			DedupRecord dedupRecord = new DedupRecord();
			dedupRecord.setId(rs.getLong("ID"));
			return dedupRecord;
		}
	}
	
	@PostConstruct
	public void initialize() throws SQLException  {
		startTime = Calendar.getInstance().getTimeInMillis();
	}
	
	@Override
	public void write(List<? extends HarvestedRecord> records) throws Exception {
		for (HarvestedRecord record : records) {
			logger.debug("About to process record: {}", record);
			processRecord(record);			
		}
	}
	
	/**
	 * process each record passed to writer;
	 * @param record
	 */
	protected void processRecord(HarvestedRecord record) {
		DedupRecord result = null;
		
		// search for possible HarvestedRecord candidates
		List<HarvestedRecord> candidateRecords = jdbcTemplate.query(
			FIND_CANDIDATES_QUERY,
			new Object[] { record.getId(), record.getIsbn(), record.getTitle() + "%" },
			HarvestedRecordLimitedRowMapper.INSTANCE
			);
		
		List<HarvestedRecord> hrToBeUpdated = new ArrayList<HarvestedRecord>();

		for (HarvestedRecord candidate: candidateRecords) {
			if (recordMatcher.matchRecords(record, candidate)) {
				List <DedupRecord> list = jdbcTemplate.query(
						FIND_EXISTING_DEDUP_RECORD_QUERY, 
						new Object[] { candidate.getId() }, 
						DedupRecordRowMapper.INSTANCE
						);
				if (list.isEmpty()) {
					hrToBeUpdated.add(candidate);
				} else {
					result = list.get(0);
				}
				locateCount++;
				break;
			}
		}
		hrToBeUpdated.add(record);
		updateLinks(hrToBeUpdated, result);
		totalCount++;
		logProgress();
	}
	
	/**
	 * create record_link for each pair of {@link DedupRecord} and {@link HarvestedRecord} in database.
	 * For deails see file src/main/resources/sql/recordmanager-create-procedures.sql
	 * @param hrList
	 * @param dedupRecord
	 */
	protected void updateLinks(List<HarvestedRecord> hrList, DedupRecord dedupRecord) {
		Long prevId = dedupRecord == null ? 0L : dedupRecord.getId();
		for(HarvestedRecord currentHr : hrList) {
			List<Long> id = jdbcTemplate.query(
					CALL_UPDATE_PROCEDURE, 
					new Object[] {prevId, currentHr.getId()},
					new LongValueRowMapper()
			);
			prevId = id != null ? id.get(0) : prevId;
		}
		
		
	}
	
	protected void logProgress() {
		if (totalCount % LOG_PERIOD == 0) {
			long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
			if (elapsedSecs == 0)
				elapsedSecs = 1;
			logger.info(String.format("Processed: %,9d, Deduplicated %,9d, processing speed %4d records/s",
							totalCount, locateCount, totalCount / elapsedSecs));
		}
	}

	

}
