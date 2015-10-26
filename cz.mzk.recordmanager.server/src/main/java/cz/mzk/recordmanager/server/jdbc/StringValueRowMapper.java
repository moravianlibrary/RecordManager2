package cz.mzk.recordmanager.server.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class StringValueRowMapper implements RowMapper<String> {

	private final int columnIndex;

	public StringValueRowMapper(int columnIndex) {
		super();
		this.columnIndex = columnIndex;
	}

	public StringValueRowMapper() {
		this(1);
	}

	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		return rs.getString(columnIndex);
	}

}
