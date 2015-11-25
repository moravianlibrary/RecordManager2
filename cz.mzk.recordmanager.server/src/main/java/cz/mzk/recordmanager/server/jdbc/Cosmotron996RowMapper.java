package cz.mzk.recordmanager.server.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.Cosmotron996;

@Component
public class Cosmotron996RowMapper implements RowMapper<Cosmotron996> {

	@Override
	public Cosmotron996 mapRow(ResultSet rs, int rowNum) throws SQLException {
		Cosmotron996 record = new Cosmotron996();
		record.setRecordId(rs.getString("record_id"));
		record.setHarvestedFrom(rs.getLong("import_conf_id"));
		record.setUpdated(rs.getDate("updated"));
		record.setDeleted(rs.getDate("deleted"));
		record.setHarvested(rs.getDate("harvested"));
		record.setRawRecord(rs.getBytes("raw_record"));
		return record;
	}

}
