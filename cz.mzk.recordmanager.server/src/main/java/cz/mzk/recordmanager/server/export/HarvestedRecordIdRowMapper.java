package cz.mzk.recordmanager.server.export;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;

public class HarvestedRecordIdRowMapper implements RowMapper<HarvestedRecordUniqueId> {

	@Override
	public HarvestedRecordUniqueId mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(rs.getLong("import_conf_id"),
				rs.getString("record_id"));
		return id;
	}

}
