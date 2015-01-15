package cz.mzk.recordmanager.server.dedup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;

@Component
public class DedupRecordLocatorImpl implements DedupRecordLocator {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	private String query = "SELECT id FROM dedup_record WHERE isbn = ?";
	
	private static enum LongRowMapper implements RowMapper<Long> {

		INSTANCE;

		@Override
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getLong(1);
		}

	}
	
	@Override
	public DedupRecord locate(HarvestedRecord record) {
		DedupRecord result = null;
		List<Long> ids = jdbcTemplate.query(query, new Object[]{ record.getIsbn() }, LongRowMapper.INSTANCE);
		if (!ids.isEmpty()) {
			return dedupRecordDao.load(ids.get(0));
		}
		return result;
	}

}
