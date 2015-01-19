package cz.mzk.recordmanager.server.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;

public class HarvestedRecordRowMapper implements RowMapper<HarvestedRecord> {

	@Autowired
	private DedupRecordDAO dedupRecordDao;
	
	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecord record = new HarvestedRecord();
		record.setId(rs.getLong("id"));
		record.setOaiRecordId(rs.getString("oai_record_id"));
		record.setRawRecord(rs.getBytes("raw_record"));
		record.setFormat(rs.getString("format"));
		record.setIsbn(rs.getString("isbn"));
		record.setTitle(rs.getString("title"));
		return record;
	}

}
