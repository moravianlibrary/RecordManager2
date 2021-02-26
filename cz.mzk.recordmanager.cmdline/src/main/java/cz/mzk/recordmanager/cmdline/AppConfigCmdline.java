package cz.mzk.recordmanager.cmdline;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import cz.mzk.recordmanager.server.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.File;

@Configuration
@PropertySource(value={"file:${CONFIG_DIR:.}/database.properties", "file:${CONFIG_DIR:.}/database.local.properties"}, ignoreResourceNotFound=true)
@Import({AppConfig.class})
public class AppConfigCmdline {

	@Bean
	public DataSource dataSource(@Value("${jdbc.driverClassName}") String driverClassName, @Value("${jdbc.url}") String url, 
			@Value("${jdbc.username}") String username, @Value("${jdbc.password}") String password) throws PropertyVetoException {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass(driverClassName);
		dataSource.setJdbcUrl(url);
		dataSource.setUser(username);
		dataSource.setPassword(password);
		dataSource.setIdleConnectionTestPeriod(30);
		dataSource.setTestConnectionOnCheckin(true);
		return dataSource;
	}

	@Bean
	public ResourceProvider resourceProvider(@Value("${CONFIG_DIR}") File configDir) {
		return new DelegatingResourceProvider( //
				new FileSystemResourceProvider(configDir), //
				new ClasspathResourceProvider() //
		);
	}

}
