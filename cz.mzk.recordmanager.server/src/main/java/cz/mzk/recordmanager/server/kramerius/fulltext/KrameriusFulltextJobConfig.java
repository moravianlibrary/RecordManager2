package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.sql.Timestamp;
import java.util.Date;
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
import cz.mzk.recordmanager.server.springbatch.UUIDIncrementer;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class KrameriusFulltextJobConfig {

	private static final Date DATE_OVERRIDEN_BY_EXPRESSION = null;

	public static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	public static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

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
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(step) //
				.end() //
				.build();
	}

	@Bean(name = "krameriusFulltextJob:step")
	public Step step() throws Exception {
		return steps
				.get("step")
				.<HarvestedRecord, HarvestedRecord>chunk(1)
				.reader(reader(LONG_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION))
				.processor(krameriusFulltextProcessor(LONG_OVERRIDEN_BY_EXPRESSION))
				.writer(krameriusFulltextWriter())
				.build();
	}

	@Bean
	public Job krameriusMissingFulltextJob(
			@Qualifier(Constants.JOB_ID_MISSING_FULLTEXT_KRAMERIUS + ":missingStep") Step missingStep) {
		return jobs.get(Constants.JOB_ID_MISSING_FULLTEXT_KRAMERIUS) //
				.validator(new KrameriusMissingFulltextJobParametersValidator()) //
				.incrementer(UUIDIncrementer.INSTANCE) //
				.listener(JobFailureListener.INSTANCE) //
				.flow(missingStep) //
				.end() //
				.build();
	}

	@Bean(name = Constants.JOB_ID_MISSING_FULLTEXT_KRAMERIUS + ":missingStep")
	public Step missingStep() throws Exception {
		return steps
				.get("step")
				.<HarvestedRecord, HarvestedRecord>chunk(1)
				.reader(missingReader(LONG_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, STRING_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION, DATE_OVERRIDEN_BY_EXPRESSION))
				.processor(krameriusFulltextProcessor(LONG_OVERRIDEN_BY_EXPRESSION))
				.writer(krameriusFulltextWriter())
				.build();
	}
	
	/* reads document uuids for given config (may be limited by update date)
	 * returns ItemReader for HarvestedRecord(s)
	 */

	@Bean(name = "krameriusFulltextJob:reader")
	@StepScope
	public ItemReader<HarvestedRecord> reader(
			@Value("#{jobParameters["
					+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE
					+ "] " + "?:jobParameters[ "
					+ Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE
					+ ']' + "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE
					+ "]}") Date to) throws Exception {

		Timestamp fromStamp = null;
		Timestamp toStamp = null;

		JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM harvested_record");

		String whereClause = "WHERE import_conf_id = :configId";
		if (from != null) {
			fromStamp = new Timestamp(from.getTime());
			whereClause += " AND updated >= :from";
		}
		if (to != null) {
			toStamp = new Timestamp(to.getTime());
			whereClause += " AND updated <= :to";
		}

		if (configId != null) {
			pqpf.setWhereClause(whereClause);
		}

		pqpf.setSortKeys(ImmutableMap.of("import_conf_id",
				Order.ASCENDING, "record_id", Order.ASCENDING));
		reader.setRowMapper(harvestedRecordRowMapper);
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		if (configId != null) {
			Map<String, Object> parameterValues = new HashMap<>();
			parameterValues.put("configId", configId);
			parameterValues.put("from", fromStamp);
			parameterValues.put("to", toStamp);
			reader.setParameterValues(parameterValues);
		}
		reader.afterPropertiesSet();

		return reader;
	}

	@Bean(name = Constants.JOB_ID_MISSING_FULLTEXT_KRAMERIUS + ":reader")
	@StepScope
	public ItemReader<HarvestedRecord> missingReader(
			@Value("#{jobParameters["
					+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FULLTEXT_FIRST + "]}") String firstId,
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FULLTEXT_LAST + "]}") String lastId,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_FROM_DATE
					+ "] " + "?:jobParameters[ "
					+ Constants.JOB_PARAM_FROM_DATE + "]}") Date from,
			@Value("#{stepExecutionContext[" + Constants.JOB_PARAM_UNTIL_DATE
					+ ']' + "?:jobParameters[" + Constants.JOB_PARAM_UNTIL_DATE
					+ "]}") Date to) throws Exception {
		JdbcPagingItemReader<HarvestedRecord> reader = new JdbcPagingItemReader<>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT *");
		pqpf.setFromClause("FROM harvested_record hr");

		String whereClause = "WHERE hr.import_conf_id = :configId AND NOT EXISTS ("
				+ "SELECT 1 FROM fulltext_kramerius fk WHERE hr.id = fk.harvested_record_id)";
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("configId", configId);
		if (from != null) {
			whereClause += " AND hr.updated >= :from";
			parameterValues.put("from", new Timestamp(from.getTime()));
		}
		if (to != null) {
			Date toStamp = new Timestamp(to.getTime());
			whereClause += " AND hr.updated <= :to";
			parameterValues.put("to", toStamp);
		}
		if (firstId != null) {
			whereClause += " AND hr.record_id >= :firstId";
			parameterValues.put("firstId", firstId);
		}
		if (lastId != null) {
			whereClause += " AND hr.record_id <= :lastId";
			parameterValues.put("lastId", lastId);
		}
		pqpf.setWhereClause(whereClause);
		pqpf.setSortKey("record_id");
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(harvestedRecordRowMapper);
		reader.setPageSize(PAGE_SIZE);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
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
