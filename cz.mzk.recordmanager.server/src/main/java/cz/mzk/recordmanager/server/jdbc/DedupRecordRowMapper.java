package cz.mzk.recordmanager.server.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.DedupRecord;

public class DedupRecordRowMapper implements RowMapper<DedupRecord>  {

	@Override
	public DedupRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		DedupRecord record = new DedupRecord();
		record.setId(rs.getLong("dedup_record_id"));
		return record;
	}

}
