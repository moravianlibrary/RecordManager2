package cz.mzk.recordmanager.server;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cz.mzk.recordmanager.server.scripting.CachingMappingResolver;
import cz.mzk.recordmanager.server.scripting.CachingStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.ClasspathMappingResolver;
import cz.mzk.recordmanager.server.scripting.ClasspathStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
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
	
	@Bean
	public PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	}

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
	
	@Bean
	public MappingResolver propertyResolver() {
		return new CachingMappingResolver(new ClasspathMappingResolver());
	}

	@Bean
	public StopWordsResolver stopWordsResolver() {
		return new CachingStopWordsResolver(new ClasspathStopWordsResolver());
	}
	
	private DatabasePopulator databasePopulator() {
	    final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
	    for (String resource : resources) {
	    	populator.addScript(new ClassPathResource(resource));
	    }
	    return populator;
	}

}
