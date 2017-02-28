package cz.mzk.recordmanager.server.index;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;

@Component
public class AdresarKnihovenRowMapper implements RowMapper<AdresarKnihoven> {

	@Override
	public AdresarKnihoven mapRow(ResultSet rs, int rowNum)	throws SQLException {
		AdresarKnihoven record = new AdresarKnihoven();
		record.setId(rs.getLong("id"));
		record.setRecordId(rs.getString("record_id"));
		record.setUpdated(rs.getDate("updated"));
		record.setRawRecord(rs.getBytes("raw_record"));
		record.setFormat(rs.getString("format"));
		return record;
	}

}
