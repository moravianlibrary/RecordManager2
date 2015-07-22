package cz.mzk.recordmanager.cmdline;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
			@Value("${jdbc.username}") String username, @Value("${jdbc.password}") String password) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

}
