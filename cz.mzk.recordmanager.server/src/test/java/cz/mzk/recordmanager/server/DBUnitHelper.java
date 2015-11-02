package cz.mzk.recordmanager.server;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.Language;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.model.Title;

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
		Statement statement = dataSource.getConnection().createStatement();
		statement.execute("truncate table " + AuthorityRecord.TABLE_NAME);
		statement.execute("truncate table " + Isbn.TABLE_NAME);
		statement.execute("truncate table " + Title.TABLE_NAME);
		statement.execute("truncate table " + Issn.TABLE_NAME);
		statement.execute("truncate table " + Cnb.TABLE_NAME);
		statement.execute("truncate table " + Language.TABLE_NAME);
		statement.execute("truncate table " + Oclc.TABLE_NAME);
		statement.execute("truncate table " + HarvestedRecordFormat.LINK_TABLE_NAME);
		statement.execute("delete from " + FulltextMonography.TABLE_NAME);
		statement.execute("delete from " + HarvestedRecord.TABLE_NAME);
		statement.execute("delete from " + OAIHarvestConfiguration.TABLE_NAME);
		statement.execute("delete from " + KrameriusConfiguration.TABLE_NAME);
		statement.execute("delete from " + DownloadImportConfiguration.TABLE_NAME);
		statement.execute("delete from " + ImportConfiguration.TABLE_NAME);
	}
	
	

}
