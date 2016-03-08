package cz.mzk.recordmanager.server.imports.inspirations;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class InspirationIdRowMapper implements RowMapper<Long> {

	@Override
	public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
		return rs.getLong("id");
	}

}
