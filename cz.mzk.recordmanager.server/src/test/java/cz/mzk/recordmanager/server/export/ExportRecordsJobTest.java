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
	
	private static final String TEST_FILE_1 = "src/test/resources/export1.txt";
	
	@BeforeMethod
	public void init() throws Exception {
		dbUnitHelper.init("dbunit/DedupRecordLocator.xml");
	}
	
	@BeforeClass
	public void cleanUp() {
		File file = new File(TEST_FILE_1);
		file.delete();
	}
	
	@Test
	public void execute() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_EXPORT);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_OUT_FILE, new JobParameter(TEST_FILE_1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}
	
}
