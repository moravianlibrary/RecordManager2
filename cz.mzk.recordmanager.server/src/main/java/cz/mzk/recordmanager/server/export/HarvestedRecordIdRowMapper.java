package cz.mzk.recordmanager.server.export;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;

public class HarvestedRecordIdRowMapper implements RowMapper<HarvestedRecordId> {

	@Override
	public HarvestedRecordId mapRow(ResultSet rs, int rowNum) throws SQLException {
		HarvestedRecordId id = new HarvestedRecordId(rs.getLong("oai_harvest_conf_id"),
				rs.getString("record_id"));
		return id;
	}

}
