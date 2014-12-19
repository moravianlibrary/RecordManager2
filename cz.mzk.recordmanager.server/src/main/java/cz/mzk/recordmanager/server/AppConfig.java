package cz.mzk.recordmanager.server;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.PlatformTransactionManager;

import cz.mzk.recordmanager.server.oai.harvest.OAIHarvestJob;

@Configuration
@EnableBatchProcessing(modular=true)
@Import(OAIHarvestJob.class)
@ImportResource("classpath:appCtx-recordmanager-server.xml")
public class AppConfig implements BatchConfigurer {

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private DataSource dataSource;

	@Bean
	public DefaultJobLoader defaultJobLoader() {
		return new DefaultJobLoader(getJobRegistry());
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor()
			throws Exception {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(getJobRegistry());
		return jobRegistryBeanPostProcessor;
	}

	@Bean
	public JobOperator jobOperator() throws Exception {
		SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobRepository(getJobRepository());
        jobOperator.setJobRegistry(getJobRegistry());
        jobOperator.setJobLauncher(getJobLauncher());
        jobOperator.setJobExplorer(getJobExplorer());
        return jobOperator;
	}

	@Override
	@Bean
	public JobExplorer getJobExplorer() throws Exception {
		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
		jobExplorerFactoryBean.setDataSource(dataSource);
		jobExplorerFactoryBean.afterPropertiesSet();
		return (JobExplorer) jobExplorerFactoryBean.getObject();
	}

	@Override
	@Bean
	public JobLauncher getJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(getJobRepository());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	@Override
	@Bean
	public PlatformTransactionManager getTransactionManager() throws Exception {
		return transactionManager;
	}

	@Override
	@Bean
	public JobRepository getJobRepository() throws Exception {
		JobRepositoryFactoryBean jobRepository = new JobRepositoryFactoryBean();
		jobRepository.setDataSource(dataSource);
		jobRepository.setTransactionManager(transactionManager);
		jobRepository.setDatabaseType("derby");
		jobRepository.afterPropertiesSet();
		return (JobRepository) jobRepository.getObject();
	}
	
    private JobRegistry getJobRegistry() {
        return new MapJobRegistry();
    }

}
