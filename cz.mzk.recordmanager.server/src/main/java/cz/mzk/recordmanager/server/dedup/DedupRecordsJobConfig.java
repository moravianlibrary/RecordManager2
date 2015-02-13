package cz.mzk.recordmanager.server.dedup;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Configuration
public class DedupRecordsJobConfig {
	
	private static Logger logger = LoggerFactory.getLogger(DedupRecordsJobConfig.class);
	
	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public Job dedupRecordsJob(@Qualifier("dedupRecordsJob:step") Step step) {
        return jobs.get("dedupRecordsJob")
        		.validator(new DedupRecordsJobParametersValidator())
				.flow(step)
				.end()
				.build();
    }
    
    @Bean(name="dedupRecordsJob:step")
    public Step step() throws Exception {
		return steps.get("dedupRecordsStep")
            .<Long, HarvestedRecord> chunk(20)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .build();
    }
	
    @Bean(name="dedupRecordsJob:reader")
	@StepScope
    public ItemReader<Long> reader() throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT hr.id");
		pqpf.setFromClause("FROM harvested_record hr LEFT JOIN record_link rl ON hr.id = rl.harvested_record_id");
		pqpf.setWhereClause("WHERE rl.harvested_record_id IS NULL");
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(20);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	reader.afterPropertiesSet();
    	return reader;
    }
    
    @Bean(name="dedupRecordsJob:processor")
	@StepScope
	public DedupKeysGeneratorProcessor processor() {
		return new DedupKeysGeneratorProcessor();
	}
    
    @Bean(name="dedupRecordsJob:writer")
	@StepScope
    public DedupRecordsWriter writer() {
    	return new DedupRecordsWriter();
    }

}
