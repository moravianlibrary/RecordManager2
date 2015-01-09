package cz.mzk.recordmanager.server;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
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
import org.springframework.transaction.PlatformTransactionManager;

import cz.mzk.recordmanager.server.dedup.DedupKeysGeneratorJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvestJobConfig;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterFactory;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterFactoryImpl;
import cz.mzk.recordmanager.server.util.ApacheHttpClient;
import cz.mzk.recordmanager.server.util.HttpClient;

@Configuration
@EnableBatchProcessing(modular=true)
@ImportResource("classpath:appCtx-recordmanager-server.xml")
public class AppConfig {

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private DataSource dataSource;

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
	public PlatformTransactionManager transactionManager() throws Exception {
		return transactionManager;
	}

	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean jobRepository = new JobRepositoryFactoryBean();
		jobRepository.setDataSource(dataSource);
		jobRepository.setTransactionManager(transactionManager);
		jobRepository.afterPropertiesSet();
		return (JobRepository) jobRepository.getObject();
	}
	
	@Bean
    public OAIHarvesterFactory oaiHarvesterFactory() {
		return new OAIHarvesterFactoryImpl();
	}
	
	@Bean
	public HttpClient httpClient() {
		return new ApacheHttpClient();
	}
	
    private JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }
    
    @Bean
    public ApplicationContextFactory moreJobs() {
    	return new GenericApplicationContextFactory(OAIHarvestJobConfig.class, DedupKeysGeneratorJobConfig.class);
    }

}
