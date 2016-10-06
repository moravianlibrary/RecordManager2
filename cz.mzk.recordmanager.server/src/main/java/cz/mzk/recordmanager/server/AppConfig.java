package cz.mzk.recordmanager.server;

import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultJobLoader;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cz.mzk.recordmanager.api.service.LibraryService;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterFactory;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterFactoryImpl;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterFactory;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterFactoryImpl;
import cz.mzk.recordmanager.server.scripting.CachingMappingResolver;
import cz.mzk.recordmanager.server.scripting.CachingStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.ResourceMappingResolver;
import cz.mzk.recordmanager.server.scripting.ResourceStopWordsResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
import cz.mzk.recordmanager.server.service.LibraryServiceImpl;
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

	private static final String DB_CHANGE_LOG = "classpath:sql/recordmanager-liquibase.sql";

	private static final String DEFAULT_LIQUIBASE_CONTEXTS = "empty";

	@Value(value = "${liquibase.enable:#{false}}")
	private boolean enableLiquibase;

	@Value(value = "${liquibase.contexts:#{null}}")
	private String liquibaseContexts;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ResourceProvider resourceProvider;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
	    return new PropertySourcesPlaceholderConfigurer();
	}

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
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
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
	public TaskExecutor taskExecutor(@Value(value = "${recordmanager.threadPoolSize}") int threadPoolSize) {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(threadPoolSize);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Override
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager();
	}

	@Bean
	public MappingResolver propertyResolver() {
		return new CachingMappingResolver(new ResourceMappingResolver(resourceProvider));
	}

	@Bean
	public StopWordsResolver stopWordsResolver() {
		return new CachingStopWordsResolver(new ResourceStopWordsResolver(resourceProvider));
	}

	@Bean
	public SpringLiquibase springLiquibase(DataSource dataSource) throws LiquibaseException {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog(DB_CHANGE_LOG);
		liquibase.setShouldRun(enableLiquibase);
		if (liquibaseContexts != null && !liquibaseContexts.trim().isEmpty()) {
			liquibase.setContexts(liquibaseContexts);
		} else {
			liquibase.setContexts(DEFAULT_LIQUIBASE_CONTEXTS);
		}
		return liquibase;
	}

	@Bean
	public LibraryService libraryService() {
		return new LibraryServiceImpl();
	}

}
