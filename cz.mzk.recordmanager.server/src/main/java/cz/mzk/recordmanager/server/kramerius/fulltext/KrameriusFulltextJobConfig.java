package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

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

import cz.mzk.recordmanager.server.index.HarvestedRecordRowMapper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class KrameriusFulltextJobConfig {

	public static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	private static final int PAGE_SIZE = 2;

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private HarvestedRecordRowMapper harvestedRecordRowMapper;
	
	@Bean
	public Job krameriusFulltextJob(
			@Qualifier("krameriusFulltextJob:step") Step step) {
		return jobs.get("krameriusFulltextJob") //
				.validator(new KrameriusFulltextJobParametersValidator()) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}
	
	@Bean(name = "krameriusFulltextJob:step")
	public Step step() throws Exception {
		return steps
				.get("step")
				.<HarvestedRecord, HarvestedRecord> chunk(1)
				.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, LONG_OVERRIDEN_BY_EXPRESSION, LONG_OVERRIDEN_BY_EXPRESSION))
				.processor(krameriusFulltextProcessor(LONG_OVERRIDEN_BY_EXPRESSION))
				.writer(krameriusFulltextWriter())
				.build();
	}
	
	
	/* reads document uuids for given config (may be limited by database HarvestedRecord ids)
	 * returns ItemReader for HarvestedRecord(s)
	 */
	
	@Bean(name = "krameriusFulltextJob:reader")
	@StepScope
	public ItemReader<HarvestedRecord> reader(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId, 
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FULLTEXT_FIRST + "]}") Long firstId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FULLTEXT_LAST + "]}") Long lastId) throws Exception {
		
		JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<HarvestedRecord>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM harvested_record");
		
		String whereClause = "WHERE import_conf_id = :configId";
		if (firstId!=null) {
			whereClause += " AND id >= :firstId";
		}
		if (lastId!=null) {
			whereClause += " AND id <= :lastId";
		}
		
		if (configId != null) {
			pqpf.setWhereClause(whereClause);
		}
				
		pqpf.setSortKeys(ImmutableMap.of("import_conf_id",
				Order.ASCENDING, "id", Order.ASCENDING));
		reader.setRowMapper(harvestedRecordRowMapper);
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (configId != null) {
			Map<String, Object> parameterValues = new HashMap<String, Object>();
			parameterValues.put("configId", configId);
			parameterValues.put("firstId", firstId);
			parameterValues.put("lastId", lastId);
			reader.setParameterValues(parameterValues);
		}
		reader.afterPropertiesSet();
		
		return reader;
	}
		
	@Bean(name = "krameriusFulltextJob:writer")
	@StepScope
	public KrameriusFulltextWriter krameriusFulltextWriter() {
		return new KrameriusFulltextWriter();
	}
	
	@Bean(name = "krameriusFulltextJob:processor")
	@StepScope
	public KrameriusFulltextProcessor krameriusFulltextProcessor(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId) {
		return new KrameriusFulltextProcessor(configId);
	}
}
