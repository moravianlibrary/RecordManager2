package cz.mzk.recordmanager.server;

import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Configuration
@EnableTransactionManagement
@PropertySource(value = {"classpath:database.test.properties", "classpath:database.test.local.properties"}, ignoreResourceNotFound = true)
public class AppConfigDev {

	private final String[] resources = {
			"sql/recordmanager-create-tables-derby.sql",
			"org/springframework/batch/core/schema-derby.sql",
			"sql/recordmanager-insert.sql",
			"sql/recordmanager-insert-test.sql",
			"sql/recordmanager-create-views.sql"
	};

	@Bean
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder
				.setType(EmbeddedDatabaseType.DERBY)
				.addScripts(resources)
				.build();
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

}
