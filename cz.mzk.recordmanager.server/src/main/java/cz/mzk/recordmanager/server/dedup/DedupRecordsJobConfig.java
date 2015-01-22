package cz.mzk.recordmanager.server.dedup;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.HibernateChunkListener;

@Configuration
public class DedupRecordsJobConfig {
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public Job dedupRecordsJob(JobBuilderFactory jobs, Step step) {
        return jobs.get("dedupRecordsJob")
				.flow(step)
				.end()
				.build();
    }
    
    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<Long> reader, ItemProcessor<Long, HarvestedRecord> processor,
    		DedupRecordsWriter writer) {
		return steps.get("step")
            .<Long, HarvestedRecord> chunk(1)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(hibernateChunkListener())
            .build();
    }
	
	@Bean
    public ItemReader<Long> reader() throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT hr.id");
		pqpf.setFromClause("FROM harvested_record hr LEFT JOIN record_link rl ON hr.id = rl.harvested_record_id");
		pqpf.setWhereClause("WHERE rl.harvested_record_id IS NULL");
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(100);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	return reader;
    }
	
	@Bean
	public DedupKeysGeneratorProcessor processor() {
		return new DedupKeysGeneratorProcessor();
	}
    
    @Bean
    public DedupRecordsWriter writer() {
    	return new DedupRecordsWriter();
    }
    
    @Bean
    public HibernateChunkListener hibernateChunkListener() {
    	return new HibernateChunkListener();
    }

}
