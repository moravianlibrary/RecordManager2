package cz.mzk.recordmanager.server.dedup;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class RegenerateDedupKeysJobConfig {

	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private DataSource dataSource;
    
    @Bean
	public Job RegenerateDedupKeysJob(
			@Qualifier(Constants.JOB_ID_REGEN_DEDUP_KEYS+":regenarateDedupKeysStep") Step regenDedupKeysStep) {
		return jobs.get(Constants.JOB_ID_REGEN_DEDUP_KEYS)
				.listener(JobFailureListener.INSTANCE).flow(regenDedupKeysStep)
				.end().build();
	}
    
    @Bean(name=Constants.JOB_ID_REGEN_DEDUP_KEYS +":regenarateDedupKeysStep")
	public Step regenerateDedupKeysStep() throws Exception {
		return steps.get("regenarateDedupKeysStep")
				.<Long, Long> chunk(20)//
				.reader(reader())//
				.writer(writer()) //
				.build();
	}
    
    @Bean(name=Constants.JOB_ID_REGEN_DEDUP_KEYS +":regenarateDedupKeysReader")
	@StepScope
    public ItemReader<Long> reader() throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM harvested_record hr");
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(20);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	reader.afterPropertiesSet();
    	return reader;
    }
    
    @Bean(name=Constants.JOB_ID_REGEN_DEDUP_KEYS +":regenarateDedupKeysWriter")
	@StepScope
	public ItemWriter<Long> writer() throws Exception {
		return new RegenerateDedupKeysWriter();
	}
}
