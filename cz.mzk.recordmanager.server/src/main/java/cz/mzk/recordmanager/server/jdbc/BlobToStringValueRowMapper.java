package cz.mzk.recordmanager.server.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class BlobToStringValueRowMapper implements RowMapper<String> {

	private final int columnIndex;

	public BlobToStringValueRowMapper(int columnIndex) {
		super();
		this.columnIndex = columnIndex;
	}

	public BlobToStringValueRowMapper() {
		this(1);
	}

	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {

// <MJ.> using of Blob caused this Exception http://stackoverflow.com/questions/2069541/postgresql-jdbc-and-streaming-blobs
//		Blob blob = rs.getBlob(columnIndex);
//		try (InputStream is = blob.getBinaryStream()) {

		try (InputStream is = rs.getBinaryStream(columnIndex)) {
			return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
		} catch (IOException ioe) {
			throw new SQLException(ioe.getMessage(), ioe);
		}
	}

}
