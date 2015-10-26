package cz.mzk.recordmanager.server;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class DBUnitHelper {

	private static final Resource DELETE_TABLES = new ClassPathResource("sql/recordmanager-delete-tables.sql");

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void dump(String outputFile) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			IDatabaseConnection conn = new DatabaseConnection(connection);
			FlatXmlWriter datasetWriter = new FlatXmlWriter(
					new FileOutputStream(outputFile));
			datasetWriter.setDocType("dataset.dtd");
			datasetWriter.write(conn.createDataSet());
		}
	}

	public void init(String resourceFile) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			IDatabaseConnection conn = new DatabaseConnection(connection);
			InputStream is = DBUnitHelper.class.getClassLoader()
					.getResourceAsStream(resourceFile);
			FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
			builder.setColumnSensing(true);
			FlatXmlDataSet dataset = builder.build(is);
			truncateTables();
			DatabaseOperation.CLEAN_INSERT.execute(conn, dataset);
		}
	}
	
	protected void truncateTables() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), DELETE_TABLES);
	}

}
