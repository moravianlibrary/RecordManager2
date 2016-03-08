package cz.mzk.recordmanager.server.imports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationDeleteJobParametersValidator;
import cz.mzk.recordmanager.server.imports.inspirations.InspirationDeleteWriter;
import cz.mzk.recordmanager.server.imports.inspirations.InspirationIdRowMapper;
import cz.mzk.recordmanager.server.springbatch.JobFailureListener;
import cz.mzk.recordmanager.server.util.Constants;

@Configuration
public class InspirationImportJobConfig {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DataSource dataSource;

	private static final String STRING_OVERRIDEN_BY_EXPRESSION = null;

	@Bean
	public Job inspirationImportJob(
			@Qualifier(Constants.JOB_ID_IMPORT_INSPIRATION +":importInspirationStep") Step inspirationImportStep) {
		return jobs.get(Constants.JOB_ID_IMPORT_INSPIRATION)
				.validator(new InspirationImportJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(inspirationImportStep)
				.end().build();
	}

	@Bean(name=Constants.JOB_ID_IMPORT_INSPIRATION +":importInspirationStep")
	public Step inspirationImportStep() throws Exception {
		return steps.get("updateRecordsJobStep")
				.<Map<String, List<String>>, Map<String, List<String>>> chunk(1)//
				.reader(inspirationFileReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(InspirationImportWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_IMPORT_INSPIRATION +":importInspirationReader")
	@StepScope
	public InspirationImportFileReader inspirationFileReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_IN_FILE + "]}") String filename)
			throws Exception {
			return new InspirationImportFileReader(filename);
	}
	
	@Bean(name=Constants.JOB_ID_IMPORT_INSPIRATION +":writer")
	@StepScope
	public InspirationImportWriter InspirationImportWriter(){
		return new InspirationImportWriter();
	}

	@Bean
	public Job inspirationDeleteJob(
			@Qualifier(Constants.JOB_ID_DELETE_INSPIRATION +":deleteInspirationStep") Step inspirationDeleteStep) {
		return jobs.get(Constants.JOB_ID_DELETE_INSPIRATION)
				.validator(new InspirationDeleteJobParametersValidator())
				.listener(JobFailureListener.INSTANCE).flow(inspirationDeleteStep)
				.end().build();
	}

	@Bean(name=Constants.JOB_ID_DELETE_INSPIRATION +":deleteInspirationStep")
	public Step inspirationDeleteStep() throws Exception {
		return steps.get("updateRecordsJobStep")
				.<Long, Long> chunk(1)//
				.reader(inspirationDeleteReader(STRING_OVERRIDEN_BY_EXPRESSION))//
				.writer(InspirationDeleteWriter()) //
				.build();
	}

	@Bean(name=Constants.JOB_ID_DELETE_INSPIRATION +":deleteInspirationReader")
	@StepScope
	public ItemReader<Long> inspirationDeleteReader(
			@Value("#{jobParameters[" + Constants.JOB_PARAM_DELETE_INSPIRATION + "]}") String name) throws Exception{
		JdbcPagingItemReader<Long> reader = new JdbcPagingItemReader<Long>();
		SqlPagingQueryProviderFactoryBean pqpf = new SqlPagingQueryProviderFactoryBean();
		pqpf.setDataSource(dataSource);
		pqpf.setSelectClause("SELECT id");
		pqpf.setFromClause("FROM inspiration");
		pqpf.setWhereClause("WHERE name = :name");
		pqpf.setSortKey("id");
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		parameterValues.put("name", name);
		reader.setParameterValues(parameterValues);
		reader.setRowMapper(new InspirationIdRowMapper());
		reader.setPageSize(20);
		reader.setQueryProvider(pqpf.getObject());
		reader.setDataSource(dataSource);
		reader.afterPropertiesSet();
		return reader;
	}
	
	@Bean(name=Constants.JOB_ID_DELETE_INSPIRATION +":writer")
	@StepScope
	public InspirationDeleteWriter InspirationDeleteWriter(){
		return new InspirationDeleteWriter();
	}	
}

