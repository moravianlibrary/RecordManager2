package cz.mzk.recordmanager.webapp;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.DelegatingResourceProvider;
import cz.mzk.recordmanager.server.FileSystemResourceProvider;
import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.webapp.controller.LibraryController;

@Configuration
@PropertySource(value={"file:${CONFIG_DIR:.}/database.properties", "file:${CONFIG_DIR:.}/database.local.properties"}, ignoreResourceNotFound=true)
@ImportResource("classpath:appCtx-recordmanager-server.xml")
@EnableWebMvc
@EnableWebSecurity(debug=true)
public class WebAppConfig extends WebSecurityConfigurerAdapter {

	@Component
	public static class SecurityWebApplicationInitializer
		extends AbstractSecurityWebApplicationInitializer {
	}

	@Autowired
	private Environment environment;

	@Value(value = "${security.admins:#{null}}")
	private String admins;

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
	public ResourceProvider resourceProvider(@Value("${CONFIG_DIR}") File configDir) {
		return new DelegatingResourceProvider( //
				new FileSystemResourceProvider(configDir), //
				new ClasspathResourceProvider() //
		);
	}

	@Bean
	public static LibraryController libraryController() {
		return new LibraryController();
	}

	@Bean
	public CommonsMultipartResolver multipartResolver(){
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
		commonsMultipartResolver.setDefaultEncoding("utf-8");
		commonsMultipartResolver.setMaxUploadSize(50000000);
		return commonsMultipartResolver;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(new ShibbolethAuthenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		List<String> adminList = Collections.emptyList();
		if (this.admins != null) {
			adminList = new ArrayList<String>();
			for (String admin : this.admins.split(",")) {
				adminList.add(admin.trim());
			}
		}
		http.addFilterBefore(new ShibbolethAuthenticationFilter(adminList),
				BasicAuthenticationFilter.class);
    }

}
