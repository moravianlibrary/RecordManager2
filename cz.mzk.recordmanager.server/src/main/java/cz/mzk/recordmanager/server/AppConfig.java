package cz.mzk.recordmanager.server;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.DefaultJobLoader;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cz.mzk.recordmanager.server.dedup.DedupRecordsJobConfig;
import cz.mzk.recordmanager.server.dedup.RegenerateDedupKeysJobConfig;
import cz.mzk.recordmanager.server.export.ExportRecordsJobConfig;
import cz.mzk.recordmanager.server.imports.ImportRecordJobConfig;
import cz.mzk.recordmanager.server.index.DeleteAllRecordsFromSolrJobConfig;
import cz.mzk.recordmanager.server.index.IndexRecordsToSolrJobConfig;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvestJobConfig;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterFactory;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterFactoryImpl;
import cz.mzk.recordmanager.server.oai.harvest.DeleteAllHarvestsJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvestJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvestSingleRecordJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterFactory;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterFactoryImpl;
import cz.mzk.recordmanager.server.scripting.CachingMappingResolver;
import cz.mzk.recordmanager.server.scripting.ClasspathMappingResolver;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl;
import cz.mzk.recordmanager.server.util.ApacheHttpClient;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HttpClient;

@Configuration
@EnableBatchProcessing(modular = true)
@EnableTransactionManagement
@ImportResource("classpath:appCtx-recordmanager-server.xml")
public class AppConfig extends DefaultBatchConfigurer {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SessionFactory sessionFactory;

	@Bean
	public DefaultJobLoader defaultJobLoader() {
		return new DefaultJobLoader(jobRegistry());
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor()
			throws Exception {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry());
		return jobRegistryBeanPostProcessor;
	}

	@Bean
	public JobOperator jobOperator() throws Exception {
		SimpleJobOperator jobOperator = new SimpleJobOperator();
		jobOperator.setJobRepository(jobRepository());
		jobOperator.setJobRegistry(jobRegistry());
		jobOperator.setJobLauncher(jobLauncher());
		jobOperator.setJobExplorer(jobExplorer());
		return jobOperator;
	}

	@Bean
	public JobExplorer jobExplorer() throws Exception {
		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
		jobExplorerFactoryBean.setDataSource(dataSource);
		jobExplorerFactoryBean.afterPropertiesSet();
		return (JobExplorer) jobExplorerFactoryBean.getObject();
	}

	@Bean
	public JobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		HibernateTransactionManager manager = new HibernateTransactionManager();
		manager.setSessionFactory(sessionFactory);
		return manager;
	}

	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean jobRepository = new JobRepositoryFactoryBean();
		jobRepository.setDataSource(dataSource);
		jobRepository.setTransactionManager(transactionManager());
		jobRepository.afterPropertiesSet();
		return (JobRepository) jobRepository.getObject();
	}

	@Bean
	public OAIHarvesterFactory oaiHarvesterFactory() {
		return new OAIHarvesterFactoryImpl();
	}
	
	@Bean
	public KrameriusHarvesterFactory krameriusHarvesterFactory() {
		return new KrameriusHarvesterFactoryImpl();
	}

	@Bean
	public HttpClient httpClient() {
		return new ApacheHttpClient();
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}

	@Bean
	public ApplicationContextFactory moreJobs() {
		return new GenericApplicationContextFactory(
				OAIHarvestJobConfig.class,
				OAIHarvestSingleRecordJobConfig.class,
				KrameriusHarvestJobConfig.class,
				DedupRecordsJobConfig.class,
				IndexRecordsToSolrJobConfig.class,
				DeleteAllHarvestsJobConfig.class,
				RegenerateDedupKeysJobConfig.class,
				ImportRecordJobConfig.class,
				ExportRecordsJobConfig.class,
				DeleteAllRecordsFromSolrJobConfig.class
			);
	}

	@Bean
	public HibernateSessionSynchronizer hibernateSessionSynchronizer() {
		return new HibernateSessionSynchronizer();
	}

	@Bean
	public SolrServerFactory solrServerFactory() {
		return new SolrServerFactoryImpl();
	}

	@Bean
	public MappingResolver propertyResolver() {
		return new CachingMappingResolver(new ClasspathMappingResolver());
	}

	@Override
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager();
	}

}
