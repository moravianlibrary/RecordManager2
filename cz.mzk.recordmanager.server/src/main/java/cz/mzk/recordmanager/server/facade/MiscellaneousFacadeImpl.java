package cz.mzk.recordmanager.server.facade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ResourceUtils;

@Component
public class MiscellaneousFacadeImpl implements MiscellaneousFacade {

	private final String lastIndexedQuery = ResourceUtils.asString("sql/query/LastGeneratedSkatKeysQuery.sql");

	private static final String DATE_STRING_SKAT_KEYS = "yyMMdd";

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public void runFilterCaslinRecordsJob() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		jobExecutor.execute(Constants.JOB_ID_FILTER_CASLIN, params);
	}

	@Override
	public void runGenerateSkatDedupKeys() {
		try {
			Map<String, JobParameter> parameters = new HashMap<>();
			parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
			LocalDateTime lastIndexed = getLastGeneratedSkatKeys(lastIndexedQuery, Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS);
			if (lastIndexed != null) {
				Date lastIndexedDate = Date.from(lastIndexed.atZone(ZoneId.systemDefault()).toInstant());
				parameters.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(lastIndexedDate));
				Calendar cal = Calendar.getInstance();
				parameters.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(cal.getTime()));
				JobParameters params = new JobParameters(parameters);
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_SKAT_KEYS);
				if (!sdf.parse(sdf.format(lastIndexedDate)).after(sdf.parse(sdf.format(cal.getTime())))) {
					jobExecutor.execute(Constants.JOB_ID_GENERATE_SKAT_DEDUP_KEYS, params);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void runZiskejLibraries() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		parameters.put(Constants.JOB_PARAM_REHARVEST, new JobParameter(Constants.JOB_PARAM_TRUE_VALUE));
		jobExecutor.execute(Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES, new JobParameters(parameters));
	}

	private LocalDateTime getLastGeneratedSkatKeys(String query, String jobName) {
		List<Date> lastGenerate = jdbcTemplate.queryForList(query, //
				ImmutableMap.of("jobName", jobName), Date.class);
		return (!lastGenerate.isEmpty() && lastGenerate.get(0) != null) ? LocalDateTime.ofInstant(lastGenerate.get(0).toInstant(), ZoneId.systemDefault()) : null;
	}

}
