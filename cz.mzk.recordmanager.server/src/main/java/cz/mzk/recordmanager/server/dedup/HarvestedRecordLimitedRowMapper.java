package cz.mzk.recordmanager.server.dedup;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.HarvestedRecord;

public enum HarvestedRecordLimitedRowMapper implements RowMapper<HarvestedRecord> {
	
	INSTANCE;

	@Override
	public HarvestedRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecord harvestedRecord = new HarvestedRecord();
		harvestedRecord.setId(rs.getLong("ID"));
		harvestedRecord.setPublicationYear(rs.getLong("PUBLICATION_YEAR"));
		harvestedRecord.setPhysicalFormat(rs.getString("PHYSICAL_FORMAT"));
		harvestedRecord.setTitle(rs.getString("TITLE"));
		harvestedRecord.setIsbn(rs.getString("ISBN"));
		return harvestedRecord;
	}
}
