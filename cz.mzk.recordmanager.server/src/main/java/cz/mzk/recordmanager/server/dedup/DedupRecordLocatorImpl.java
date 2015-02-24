package cz.mzk.recordmanager.server.dedup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvester;

@Component
public class DedupRecordLocatorImpl implements DedupRecordLocator {

	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	private String query = "SELECT * FROM dedup_record WHERE isbn = ? OR title = ?";

	private static final int TITLE_MATCH_TRESHOLD = 50;
	
	private static enum DedupRecordRowMapper implements RowMapper<DedupRecord> {

		INSTANCE;

		@Override
		public DedupRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
			DedupRecord dedupRecord = new DedupRecord();
			dedupRecord.setId(rs.getLong("ID"));
			dedupRecord.setPublicationYear(rs.getLong("PUBLICATION_YEAR"));
			dedupRecord.setPhysicalFormat(rs.getString("PHYSICAL_FORMAT"));
			dedupRecord.setTitle(rs.getString("TITLE"));
			dedupRecord.setIsbn(rs.getString("ISBN"));
			return dedupRecord;
		}

	}
	
	@Override
	public DedupRecord locate(HarvestedRecord record) {
		DedupRecord result = null;
		List<DedupRecord> records = jdbcTemplate.query(query, new Object[]{ record.getIsbn(), record.getTitle() }, DedupRecordRowMapper.INSTANCE);
		for (DedupRecord rec: records) {
			if (matchRecords(record, rec)) {
				return rec;
			}
		}
		return result;
	}
	
	protected boolean matchRecords(final HarvestedRecord origRecord, final DedupRecord dedupRecord) {
		boolean isbnMatch = false;
		boolean titleMatch = false;
		
		if (origRecord.getPublicationYear() != null 
				&& dedupRecord.getPublicationYear() != null 
				&& !origRecord.getPublicationYear().equals(dedupRecord.getPublicationYear())) {
			logger.debug("Publication year mismatch, original: {} deduplicated: {}", origRecord, dedupRecord);
			return false;
		}
				
		if (origRecord.getPhysicalFormat() != null 
				&& dedupRecord.getPhysicalFormat() != null 
				&& !origRecord.getPhysicalFormat().equals(dedupRecord.getPhysicalFormat())) {
			logger.debug("Format mismatch, original: {} deduplicated: {}", origRecord, dedupRecord);
			return false;
		}
		
		if (origRecord.getIsbn() != null && dedupRecord.getIsbn() != null) {
			isbnMatch = origRecord.getIsbn().equals(dedupRecord.getIsbn());
		}
		
		if (origRecord.getTitle() != null && dedupRecord.getTitle() != null) {
			if (levensteinTitleMatch(origRecord.getTitle(), dedupRecord.getTitle()) > TITLE_MATCH_TRESHOLD) {
				titleMatch = true;
			}
		}
		
		if (titleMatch && isbnMatch) {
			return true;
		}
		
		return deepMatchRecords(origRecord, dedupRecord);
	}
	
	protected boolean deepMatchRecords(final HarvestedRecord origrRecord, final DedupRecord dedupRecord) {
		//TODO implementation
		return false;
	}
	
	/**
	 * @param origTitle
	 * @param dedupTitle
	 * @return percentage of match based of Levenstein distance (0 - 100)
	 */
	protected int levensteinTitleMatch(final String origTitle, final String dedupTitle) {
		if (origTitle == null || dedupTitle == null) {
			return 0;
		}
		int dist = StringUtils.getLevenshteinDistance(origTitle, dedupTitle);
		return 100 - (dist /  Math.min(origTitle.length(), dedupTitle.length())) ;
	}

}
