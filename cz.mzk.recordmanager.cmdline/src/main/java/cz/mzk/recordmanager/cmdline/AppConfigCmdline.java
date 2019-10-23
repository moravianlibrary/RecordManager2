package cz.mzk.recordmanager.cmdline;

import java.beans.PropertyVetoException;
import java.io.File;

import javax.sql.DataSource;

import cz.mzk.recordmanager.server.bibliolinker.BiblioLinkerJobConfig;
import cz.mzk.recordmanager.server.bibliolinker.keys.RegenerateBiblioLinkerKeysJobConfig;
import cz.mzk.recordmanager.server.imports.obalky.annotations.AnnotationsHarvestJobConfig;
import cz.mzk.recordmanager.server.miscellaneous.caslin.view.CaslinViewJobsConfig;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import cz.mzk.recordmanager.server.AppConfig;
import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.DelegatingResourceProvider;
import cz.mzk.recordmanager.server.FileSystemResourceProvider;
import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.server.adresar.AdresarHarvestJobConfig;
import cz.mzk.recordmanager.server.dedup.DedupRecordsJobConfig;
import cz.mzk.recordmanager.server.dedup.RegenerateDedupKeysJobConfig;
import cz.mzk.recordmanager.server.export.ExportRecordsJobConfig;
import cz.mzk.recordmanager.server.imports.ImportRecordJobConfig;
import cz.mzk.recordmanager.server.imports.manuscriptorium.ManuscriptoriumFulltextJobConfig;
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
import cz.mzk.recordmanager.server.miscellaneous.agrovoc.AgrovocConvertorJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.cosmotron.CosmotronHarvestJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.DeleteAllHarvestsJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvestJobConfig;

@Configuration
@PropertySource(value={"file:${CONFIG_DIR:.}/database.properties", "file:${CONFIG_DIR:.}/database.local.properties"}, ignoreResourceNotFound=true)
@Import({AppConfig.class})
public class AppConfigCmdline {

	@Autowired
	private Environment environment;

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
				ManuscriptoriumFulltextJobConfig.class,
				AgrovocConvertorJobConfig.class,
				CaslinViewJobsConfig.class,
				AnnotationsHarvestJobConfig.class,
				BiblioLinkerJobConfig.class,
				RegenerateBiblioLinkerKeysJobConfig.class
			);
	}

}
