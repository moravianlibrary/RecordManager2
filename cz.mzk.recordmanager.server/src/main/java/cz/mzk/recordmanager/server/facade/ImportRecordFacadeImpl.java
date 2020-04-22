package cz.mzk.recordmanager.server.facade;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import cz.mzk.recordmanager.server.util.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.facade.exception.JobExecutionFailure;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.TarGzUtils;

@Component
public class ImportRecordFacadeImpl implements ImportRecordFacade {

	private static Logger logger = LoggerFactory.getLogger(ImportRecordFacadeImpl.class);
	
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	private static final Pattern LOCAL_IMPORT = Pattern.compile("^local:(.*/)([^/]*)");
	
	private List<String> files = null;

	private final String lastJobExecutionQuery = ResourceUtils.asString("sql/query/LastJobExecutionQuery.sql");

	@Override
	public void importFactory(DownloadImportConfiguration dic) {
		if (dic.getUrl() != null && LOCAL_IMPORT.matcher(dic.getUrl()).matches()) {
			unpackAndImportRecordsJob(dic);
		}
		else {
			switch (dic.getJobName()) {
			case Constants.JOB_ID_IMPORT_ANTIKVARIATY:
			case Constants.JOB_ID_DOWNLOAD_IMPORT:
				downloadAndImportRecordSJob(dic);
				break;
			}
		}
	}
	
	@Override
	public void importFile(long importConfId, File file, String format) {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(file.getAbsolutePath()));
		parameters.put(Constants.JOB_PARAM_FORMAT, new JobParameter(format));
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(importConfId));
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_IMPORT, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Incremental harvest failed", exec);
		}
	}

	@Override
	public void downloadAndImportRecordSJob(DownloadImportConfiguration dic) {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(dic.getId()));
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(dic.getJobName(), params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Download and import failed", exec);
		}
	}

	@Override
	public void importOaiRecordsJob(long impotrConfId, String fileName) {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(impotrConfId));
		parameters.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(fileName));
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_IMPORT_OAI, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("ImportOaiRecords failed", exec);
		}
	}
	
	@Override
	public void unpackAndImportRecordsJob(DownloadImportConfiguration dic) {
		Matcher matcher = LOCAL_IMPORT.matcher(dic.getUrl());
		if (matcher.matches()) {
			getFileNames(matcher.group(1), matcher.group(2));
			File destFile = new File(matcher.group(1), dic.getIdPrefix());
			deleteFile(destFile);
			for (String tarFileName: files) {
				File tarFile = new File(matcher.group(1), tarFileName);
				try {
					TarGzUtils.extract(tarFile, destFile);
					logger.info("Importing file: " + tarFileName);
					
					switch (dic.getJobName()) {
					case Constants.JOB_ID_IMPORT_OAI:
						importOaiRecordsJob(dic.getId(), destFile.getAbsolutePath());
						break;
					default:
						importFile(dic.getId(), destFile, dic.getFormat());
						break;
					}
					
					deleteFile(destFile);
					deleteFile(tarFile);
				} catch (Exception e) {
					deleteFile(destFile);
					logger.info("Importing FAILED: " + tarFileName);
					e.printStackTrace();
					break;
				}
			}
		}
	}

	@Override
	public void harvestInspirationsJob() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_IMPORT_INSPIRATION, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure(Constants.JOB_ID_IMPORT_INSPIRATION + " failed", exec);
		}
	}

	private void deleteFile(File file) {
		logger.info("Delete file: " + file.getAbsolutePath());
		FileUtils.deleteQuietly(file);
	}
	
	private void getFileNames(String dirName, String fileNamePrefix) {
		files = new ArrayList<>();
		File f = new File(dirName);
		String[] namesArray = f.list();
		Arrays.sort(namesArray);
		for (String fileName: namesArray) {
			if (fileName.startsWith(fileNamePrefix)) {
				files.add(fileName);
			}
		}
	}

	private LocalDateTime query(String query, Map<String, ?> params) {
		List<Date> lastIndex = jdbcTemplate.queryForList(query, params, Date.class);
		return (!lastIndex.isEmpty() && lastIndex.get(0) != null) ? LocalDateTime.ofInstant(lastIndex.get(0).toInstant(), ZoneId.systemDefault()) : null;
	}

	@Override
	public LocalDateTime getLastCompletedExecution(String jobName) {
		return query(lastJobExecutionQuery, ImmutableMap.of("jobName", jobName));
	}

	@Override
	public void reharvestAntikvariaty() {
		Map<String, JobParameter> parameters = new HashMap<>();
		parameters.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(Constants.IMPORT_CONF_ID_ANTIKVARIATY));
		parameters.put(Constants.JOB_PARAM_REPEAT, new JobParameter(Constants.JOB_PARAM_ONE_VALUE));
		parameters.put(Constants.JOB_PARAM_REHARVEST, new JobParameter(Constants.JOB_PARAM_TRUE_VALUE));
		JobParameters params = new JobParameters(parameters);
		JobExecution exec = jobExecutor.execute(Constants.JOB_ID_IMPORT_ANTIKVARIATY, params);
		if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
			throw new JobExecutionFailure("Reharvest failed", exec);
		}
	}

}
