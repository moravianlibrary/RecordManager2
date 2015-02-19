package cz.mzk.recordmanager.cmdline;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource(value={"file:${CONFIG_DIR}/database.properties", "file:${CONFIG_DIR}/database.local.properties"}, ignoreResourceNotFound=true)
public class AppConfigCmdline {
	
	@Autowired
	private Environment environment;
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(environment.getProperty("jdbc.driverClassName"));
		dataSource.setUrl(environment.getProperty("jdbc.url"));
		dataSource.setPassword(environment.getProperty("jdbc.username"));
		dataSource.setUsername(environment.getProperty("jdbc.password"));
		return dataSource;
	}

}
