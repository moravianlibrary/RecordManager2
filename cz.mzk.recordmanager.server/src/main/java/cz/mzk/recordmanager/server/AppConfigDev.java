package cz.mzk.recordmanager.server;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@PropertySource(value={"classpath:database.test.properties", "classpath:database.test.local.properties"}, ignoreResourceNotFound=true)
public class AppConfigDev {
	
	private List<String> resources = Arrays.asList(
			"sql/recordmanager-create-tables.sql",
			"org/springframework/batch/core/schema-derby.sql",
			"sql/recordmanager-insert-test.sql"
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
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dataSource.setUrl("jdbc:derby:memory:recordmanager;create=true");
		dataSource.setPassword("recordmanager");
		dataSource.setUsername("recordmanager");
		DataSourceInitializer init = dataSourceInitializer(dataSource);
		init.afterPropertiesSet();
		return dataSource;
	}
	
	private DatabasePopulator databasePopulator() {
	    final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
	    for (String resource : resources) {
	    	populator.addScript(new ClassPathResource(resource));
	    }
	    return populator;
	}

}
