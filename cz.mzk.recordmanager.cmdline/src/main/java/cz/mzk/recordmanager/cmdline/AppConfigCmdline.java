package cz.mzk.recordmanager.cmdline;

import java.beans.PropertyVetoException;
import java.io.File;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.DelegatingResourceProvider;
import cz.mzk.recordmanager.server.FileSystemResourceProvider;
import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.server.scripting.CachingMappingResolver;
import cz.mzk.recordmanager.server.scripting.CachingStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.ClasspathMappingResolver;
import cz.mzk.recordmanager.server.scripting.ClasspathStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.FileSystemMappingResolver;
import cz.mzk.recordmanager.server.scripting.FileSystemStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;

@Configuration
@PropertySource(value={"file:${CONFIG_DIR:.}/database.properties", "file:${CONFIG_DIR:.}/database.local.properties"}, ignoreResourceNotFound=true)
public class AppConfigCmdline {

	@Autowired
	private Environment environment;

	@Bean
	public PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public DataSource dataSource(@Value("${jdbc.driverClassName}") String driverClassName, @Value("${jdbc.url}") String url, 
			@Value("${jdbc.username}") String username, @Value("${jdbc.password}") String password) throws PropertyVetoException {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass(driverClassName);
		dataSource.setJdbcUrl(url);
		dataSource.setUser(username);
		dataSource.setPassword(password);
		return dataSource;
	}
	
	@Bean
	public MappingResolver mappingResolver(@Value("${CONFIG_DIR}") File configDir) {
		return new CachingMappingResolver( //
				new FileSystemMappingResolver(new File(configDir, "mapping")), //
				new ClasspathMappingResolver() //
		);
	}

	@Bean
	public StopWordsResolver stopWordsResolver(@Value("${CONFIG_DIR}") File configDir) {
		return new CachingStopWordsResolver( //
				new FileSystemStopWordsResolver(new File(configDir, "stopwords")), //
				new ClasspathStopWordsResolver() //
		);
	}

	@Bean
	public ResourceProvider resourceProvider(@Value("${CONFIG_DIR}") File configDir) {
		return new DelegatingResourceProvider( //
				new FileSystemResourceProvider(configDir), //
				new ClasspathResourceProvider() //
		);
	}

}
