package cz.mzk.recordmanager.server.imports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.marc4j.marc.Record;
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
public class ZakonyProLidiHarvestJobConfig {
	
	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private DataSource dataSource;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;
	
	// harvest metadata
	@Bean
	public Job zakonyProLidiHarvestJob(
			@Qualifier(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":step") Step step) {
		return jobs.get(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI) //
				.validator(new ZakonyProLidiHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":step")
	public Step step() throws Exception {
		return steps.get(Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":step")
				.<List<Record>, List<Record>> chunk(10)//
				.reader(importZakonyProLidiReader())//
				.writer(importZakonyProLidiWriter(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":reader")
	@StepScope
	public ItemReader<List<Record>> importZakonyProLidiReader() {
		return new ZakonyProLidiRecordsReader();
	}

	@Bean(name=Constants.JOB_ID_HARVEST_ZAKONYPROLIDI + ":writer")
	@StepScope
	public ImportRecordsWriter importZakonyProLidiWriter(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configurationId) {
		return new ImportRecordsWriter(configurationId);
	}

	// fulltext harvest
	@Bean
	public Job zakonyProLidiFulltextJob(
			@Qualifier(Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI + ":fulltextStep") Step fulltextStep) {
		return jobs.get(Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI) //
				.validator(new ZakonyProLidiHarvestJobParametersValidator())
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(fulltextStep) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI + ":fulltextStep")
	public Step fulltextStep() throws Exception {
		return steps.get(Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI + ":fulltextStep")
				.<HarvestedRecordUniqueId, HarvestedRecordUniqueId> chunk(1)//
				.reader(zakonyProLidiFulltextReader(LONG_OVERRIDEN_BY_EXPRESSION))//
				.writer(zakonyProLidiFulltextWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI + ":reader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> zakonyProLidiFulltextReader(@Value("#{jobParameters[" + Constants.JOB_PARAM_CONF_ID + "]}") Long configId)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<HarvestedRecordUniqueId>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id and deleted is null");
		pqpf.setSortKeys(ImmutableMap.of("record_id", Order.DESCENDING));
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

	@Bean(name=Constants.JOB_ID_FULLTEXT_ZAKONYPROLIDI + ":writer")
	@StepScope
	public ZakonyProLidiFulltextWriter zakonyProLidiFulltextWriter() {
		return new ZakonyProLidiFulltextWriter();
	}
	
}
