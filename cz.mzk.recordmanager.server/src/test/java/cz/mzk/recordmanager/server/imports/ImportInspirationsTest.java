package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordInspirationDAO;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ImportInspirationsTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HarvestedRecordInspirationDAO insDao;

	@Autowired
	private InspirationDAO insNameDao;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/InspirationRecords.xml");
	}

	@Test
	public void inspirationTest() throws Exception {
		String file = this.getClass().getResource("/import/inspiration/inspirations.json").getFile();

		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_INSPIRATION);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(file));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertEquals(insNameDao.findAll().size(), 3);
		job = jobRegistry.getJob(Constants.JOB_ID_DELETE_INSPIRATION);
		params = new HashMap<>();
		params.put(Constants.JOB_PARAM_DELETE_INSPIRATION, new JobParameter("summer2016"));
		jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
		Assert.assertEquals(insDao.findAll().size(), 2);
	}

}
