package cz.mzk.recordmanager.server.export;

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
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class ExportRecordsJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	private static final Long LONG_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job exportRecordsJob(
			@Qualifier("exportRecordsJob:exportRecordsStep") Step exportRecordsStep) {
		return jobs.get(Constants.JOB_ID_EXPORT)
				.validator(new ExportRecordsJobParametersValidator())
				.listener(JobFailureListener.INSTANCE)
				.flow(exportRecordsStep)
				.end()
				.build();
	}

	@Bean(name = "exportRecordsJob:exportRecordsStep")
	public Step exportRecordsStep() throws Exception {
		return steps.get("updateRecordsJobStep")
				.<HarvestedRecordUniqueId, String> chunk(20)//
				.reader(exportRecordsReader(LONG_OVERRIDEN_BY_EXPRESSION)) //
				.processor(exportRecordsProcessor(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.writer(exportRecordsWriter(STRING_OVERRIDEN_BY_EXPRESSION)) //
				.build();
	}

	@Bean(name = "exportRecordsJob:exportRecordsReader")
	@StepScope
	public ItemReader<HarvestedRecordUniqueId> exportRecordsReader(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_CONF_ID + "]}") Long configId)
			throws Exception {
		JdbcPagingItemReader<HarvestedRecordUniqueId> reader = new JdbcPagingItemReader<HarvestedRecordUniqueId>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT import_conf_id, record_id");
		pqpf.setFromClause("FROM harvested_record");
		pqpf.setWhereClause("WHERE import_conf_id = :conf_id");
		pqpf.setSortKeys(ImmutableMap.of("import_conf_id", Order.ASCENDING, "record_id", Order.ASCENDING));
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		parameterValues.put("conf_id", configId);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new HarvestedRecordIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}

	@Bean(name = "exportRecordsJob:exportRecordsProcesor")
	@StepScope
	public ExportRecordsProcessor exportRecordsProcessor(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_FORMAT + "]}") String strFormat) {
		IOFormat iOFormat = IOFormat
				.stringToExportFormat(strFormat);
		return new ExportRecordsProcessor(iOFormat);
	}

	@Bean(name = "exportRecordsJob:exportRecordsWriter")
	@StepScope
	public FlatFileItemWriter<String> exportRecordsWriter(@Value("#{jobParameters["
			+ Constants.JOB_PARAM_OUT_FILE + "]}") String filename) throws Exception {
		FlatFileItemWriter<String> fileWritter = new FlatFileItemWriter<String>();
		fileWritter.setAppendAllowed(false);
		fileWritter.setShouldDeleteIfExists(true);
		fileWritter.setEncoding("UTF-8");
		fileWritter.setLineAggregator(new PassThroughLineAggregator<String>());
		fileWritter.setResource(new FileSystemResource(filename));
		fileWritter.afterPropertiesSet();
		return fileWritter;
	}

}
