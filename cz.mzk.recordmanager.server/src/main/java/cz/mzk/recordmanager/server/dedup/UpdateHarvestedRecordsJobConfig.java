package cz.mzk.recordmanager.server.dedup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.jdbc.LongValueRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.IntrospectiveJobParametersValidator;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;

@Configuration
public class UpdateHarvestedRecordsJobConfig {

	private static enum DedupKeysJobParametersValidator implements IntrospectiveJobParametersValidator {

		INSTANCE;

		@Override
		public void validate(JobParameters parameters)
				throws JobParametersInvalidException {
		}

		@Override
		public Collection<JobParameterDeclaration> getParameters() {
			return Collections.emptyList();
		}

	}

	public static final String OAI_HARVEST_CONF_ID = "oaiHarvestConfId";

	public static final String JOB_ID = "updateHarvestedRecordsJob";

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	@Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private DataSource dataSource;

    @Bean
    public Job updateHarvestedRecordsJob(@Qualifier("updateHarvestedRecords:step") Step step) {
        return jobs.get(JOB_ID)
        		.validator(DedupKeysJobParametersValidator.INSTANCE)
        		.listener(JobFailureListener.INSTANCE)
				.flow(step)
				.end()
				.build();
    }

    @Bean(name="updateHarvestedRecords:step")
    public Step updateRecordsStep() throws Exception {
		return steps.get("dedupKeysGeneratorJobStep")
            .<Long, HarvestedRecord> chunk(20) //
            .reader(reader(LONG_OVERRIDEN_BY_EXPRESSION)) //
            .processor(processor()) //
            .writer(writer()) //
            .build();
    }

    @Bean(name="updateHarvestedRecords:reader")
	@StepScope
    public ItemReader<Long> reader(@Value("#{jobParameters[" + OAI_HARVEST_CONF_ID  + "]}") Long oaiHarvestConfId) throws Exception {
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT hr.id");
		pqpf.setFromClause("FROM harvested_record hr");
		if (oaiHarvestConfId != null) {
			pqpf.setWhereClause("WHERE oai_harvest_conf_id = :oai_harvest_conf_id");
		}
		pqpf.setSortKey("id");
		reader.setRowMapper(new LongValueRowMapper());
		reader.setPageSize(20);
    	reader.setQueryProvider(pqpf.getObject());
    	reader.setDataSource(dataSource);
    	if (oaiHarvestConfId != null) {
    		Map<String, Object> parameterValues = new HashMap<String, Object>();
    		parameterValues.put("oai_harvest_conf_id", oaiHarvestConfId);
    		reader.setParameterValues(parameterValues);
    	}
    	reader.afterPropertiesSet();
    	return reader;
    }

    @Bean(name="updateHarvestedRecords:processor")
	@StepScope
    public ItemProcessor<Long, HarvestedRecord> processor() throws Exception {
    	return new UpdateHarvestedRecordProcessor();
    }

    @Bean(name="updateHarvestedRecords:writer")
	@StepScope
    public ItemWriter<HarvestedRecord> writer() throws Exception {
    	return new HarvestedRecordWriter();
    }

}
