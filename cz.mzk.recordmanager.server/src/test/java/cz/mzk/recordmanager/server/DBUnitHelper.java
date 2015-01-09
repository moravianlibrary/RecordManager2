package cz.mzk.recordmanager.server;

import java.io.FileOutputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBUnitHelper {

	@Autowired
	private DataSource dataSource;

	public void dump(String outputFile) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			IDatabaseConnection conn = new DatabaseConnection(connection);
			FlatXmlWriter datasetWriter = new FlatXmlWriter(
					new FileOutputStream(outputFile));
			datasetWriter.setDocType("dataset.dtd");
			datasetWriter.write(conn.createDataSet());
		}
	}

}
