package cz.mzk.recordmanager.server.export;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;
import cz.mzk.recordmanager.server.util.Constants;

public class ExportRecordsJobTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private DBUnitHelper dbUnitHelper;

	private static final String TEST_FILE_1 = "target/test/export1.txt";
	private static final String TEST_FILE_2 = "target/test/export_iso2709.txt";
	private static final String TEST_FILE_3 = "target/test/export_aleph.txt";
	private static final String TEST_FILE_4 = "target/test/export.txt";
	private static final String TEST_FILE_5 = "target/test/exportDC.txt";

	@BeforeMethod
	public void init() throws Exception {
		dbUnitHelper.init("dbunit/ExportRecords.xml");
	}

	@BeforeClass
	public void cleanUp() {
		for (String filename : new String[]{TEST_FILE_1, TEST_FILE_2, TEST_FILE_3, TEST_FILE_4, TEST_FILE_5}) {
			File file = new File(filename);
			if (!file.delete()) logger.debug(String.format("file %s doesn't exist", filename));
		}
	}

	@Test
	public void execute() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_EXPORT);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_OUT_FILE, new JobParameter(TEST_FILE_1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

	@Test
	public void testExportISO2709() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_EXPORT);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_OUT_FILE, new JobParameter(TEST_FILE_2));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("iso"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

	@Test
	public void testExportAleph() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_EXPORT);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_OUT_FILE, new JobParameter(TEST_FILE_3));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("aleph"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

	@Test
	public void testExportDublicCore() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_EXPORT);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(304L));
		params.put(Constants.JOB_PARAM_OUT_FILE, new JobParameter(TEST_FILE_5));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("dcxml"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

	/**
	 * test export marc record with alphabetic field identifier
	 */
	@Test
	public void testExportRecordsMarcAlpnaKey() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_EXPORT);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(301L));
		params.put(Constants.JOB_PARAM_OUT_FILE, new JobParameter(TEST_FILE_4));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("xml"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

}
