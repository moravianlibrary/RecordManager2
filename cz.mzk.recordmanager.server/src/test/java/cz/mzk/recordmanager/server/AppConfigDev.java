package cz.mzk.recordmanager.server;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import cz.mzk.recordmanager.server.marc.ItemIdTest;
import cz.mzk.recordmanager.server.marc.MarcInterceptionTest;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.easymock.EasyMock;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cz.mzk.recordmanager.server.adresar.AdresarHarvestJobConfig;
import cz.mzk.recordmanager.server.dedup.DedupRecordsJobConfig;
import cz.mzk.recordmanager.server.dedup.RegenerateDedupKeysJobConfig;
import cz.mzk.recordmanager.server.export.ExportRecordsJobConfig;
import cz.mzk.recordmanager.server.imports.ImportRecordJobConfig;
import cz.mzk.recordmanager.server.imports.obalky.ObalkyKnihHarvestJobConfig;
import cz.mzk.recordmanager.server.imports.zakony.ZakonyProLidiHarvestJobConfig;
import cz.mzk.recordmanager.server.imports.inspirations.InspirationImportJobConfig;
import cz.mzk.recordmanager.server.index.DeleteAllRecordsFromSolrJobConfig;
import cz.mzk.recordmanager.server.index.IndexHarvestedRecordsToSolrJobConfig;
import cz.mzk.recordmanager.server.index.IndexRecordsToSolrJobConfig;
import cz.mzk.recordmanager.server.kramerius.fulltext.KrameriusFulltextJobConfig;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvestJobConfig;
import cz.mzk.recordmanager.server.miscellaneous.caslin.filter.FilterCaslinRecordsBySiglaJobConfig;
import cz.mzk.recordmanager.server.miscellaneous.MiscellaneousJobsConfig;
import cz.mzk.recordmanager.server.oai.harvest.cosmotron.CosmotronHarvestJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.DeleteAllHarvestsJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvestJobConfig;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;

@Configuration
@EnableTransactionManagement
@PropertySource(value={"classpath:database.test.properties", "classpath:database.test.local.properties"}, ignoreResourceNotFound=true)
public class AppConfigDev {
	
	private List<String> resources = Arrays.asList(
			"sql/recordmanager-create-tables-derby.sql",
			"org/springframework/batch/core/schema-derby.sql",
			"sql/recordmanager-insert.sql",
			"sql/recordmanager-insert-test.sql",
			"sql/recordmanager-create-views.sql"
	);

	public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
	    final DataSourceInitializer initializer = new DataSourceInitializer();
	    initializer.setDataSource(dataSource);
	    initializer.setDatabasePopulator(databasePopulator());
	    return initializer;
	}
	
	@Bean
	public DataSource dataSource(@Value("${jdbc.driverClassName}") String driverClassName, @Value("${jdbc.url}") String url, 
			@Value("${jdbc.username}") String username, @Value("${jdbc.password}") String password) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setPassword(username);
		dataSource.setUsername(password);
		DataSourceInitializer init = dataSourceInitializer(dataSource);
		init.afterPropertiesSet();
		return dataSource;
	}
	
	@Bean
	@Primary
	public HttpClient mockedHttpClient() {
		return EasyMock.createStrictMock(HttpClient.class);
	}
	
	@Bean
	@Primary
    public SolrServerFactory mockedSolrServerFactory() {
    	return EasyMock.createMock(SolrServerFactory.class);
    }

	@Bean(destroyMethod = "shutdown")
	public EmbeddedSolrServer embeddedSolrServer() throws IOException {
		File solrHome = new File("src/test/resources/solr/cores/");
		File configFile = new File(solrHome, "solr.xml");
		CoreContainer container = CoreContainer.createAndLoad(
				solrHome.getCanonicalPath(), configFile);
		return new EmbeddedSolrServer(container, "biblio");
	}

	@Bean
	public ResourceProvider resourceProvider() {
		return new ClasspathResourceProvider();
	}

	@Bean
	public ApplicationContextFactory moreJobs() {
		return new GenericApplicationContextFactory(
				OAIHarvestJobConfig.class,
				KrameriusFulltextJobConfig.class,
				KrameriusHarvestJobConfig.class,
				CosmotronHarvestJobConfig.class,
				DedupRecordsJobConfig.class,
				IndexRecordsToSolrJobConfig.class,
				DeleteAllHarvestsJobConfig.class,
				RegenerateDedupKeysJobConfig.class,
				ImportRecordJobConfig.class,
				InspirationImportJobConfig.class,
				ExportRecordsJobConfig.class,
				DeleteAllRecordsFromSolrJobConfig.class,
				MiscellaneousJobsConfig.class,
				IndexHarvestedRecordsToSolrJobConfig.class,
				ObalkyKnihHarvestJobConfig.class,
				FilterCaslinRecordsBySiglaJobConfig.class,
				ZakonyProLidiHarvestJobConfig.class,
				AdresarHarvestJobConfig.class,
				MarcInterceptionTest.class,
				ItemIdTest.class
			);
	}

	private DatabasePopulator databasePopulator() {
	    final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
	    for (String resource : resources) {
	    	populator.addScript(new ClassPathResource(resource));
	    }
	    return populator;
	}

}
