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
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;
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
				.<HarvestedRecordId, HarvestedRecordId> chunk(20)//
				.reader(reader())//
				.writer(writer()) //
				.build();
	}
    
    @Bean(name=Constants.JOB_ID_REGEN_DEDUP_KEYS +":regenarateDedupKeysReader")
	@StepScope
    public ItemReader<HarvestedRecordId> reader() throws Exception {
		JdbcPagingItemReader<HarvestedRecordId> reader = new JdbcPagingItemReader<HarvestedRecordId>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT oai_harvest_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record hr");
		pqpf.setSortKeys(ImmutableMap.of("oai_harvest_conf_id", Order.ASCENDING, "record_id", Order.ASCENDING));
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(20);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	reader.afterPropertiesSet();
    	return reader;
    }
    
    @Bean(name=Constants.JOB_ID_REGEN_DEDUP_KEYS +":regenarateDedupKeysWriter")
	@StepScope
	public ItemWriter<HarvestedRecordId> writer() throws Exception {
		return new RegenerateDedupKeysWriter();
	}
}
