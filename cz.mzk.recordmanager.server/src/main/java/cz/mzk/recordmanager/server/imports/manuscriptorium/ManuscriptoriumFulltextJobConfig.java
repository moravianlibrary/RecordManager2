package cz.mzk.recordmanager.server.imports.manuscriptorium;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import cz.mzk.recordmanager.server.imports.zakony.ZakonyProLidiHarvestJobParametersValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.export.HarvestedRecordIdRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class ManuscriptoriumFulltextJobConfig {
	
	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private DataSource dataSource;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	// fulltext harvest
	@Bean
	public Job manuscriptoriumFulltextJob(
			@Qualifier(Constants.JOB_ID_FULLTEXT_MANUSCRIPTORIUM + ":fulltextStep") Step fulltextStep) {
		return jobs.get(Constants.JOB_ID_FULLTEXT_MANUSCRIPTORIUM) //
				.validator(new ZakonyProLidiHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(fulltextStep) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_FULLTEXT_MANUSCRIPTORIUM + ":fulltextStep")
	public Step fulltextStep() throws Exception {
		return steps.get(Constants.JOB_ID_FULLTEXT_MANUSCRIPTORIUM + ":fulltextStep")
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId> chunk(10)//
				.reader(manuscriptoriumFulltextReader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(manuscriptoriumFulltextWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_FULLTEXT_MANUSCRIPTORIUM + ":reader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> manuscriptoriumFulltextReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<HarvestedRecordUniqueId>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id and deleted is null");
		pqpf.setSortKeys(ImmutableMap.of("record_id", Order.ASCENDING));
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		parameterValues.put("conf_id", configId);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(1);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name=Constants.JOB_ID_FULLTEXT_MANUSCRIPTORIUM + ":writer")
	@StepScope
	public ManuscriptoriumFulltextWriter manuscriptoriumFulltextWriter() {
		return new ManuscriptoriumFulltextWriter();
	}
	
}
