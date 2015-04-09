package cz.mzk.recordmanager.server.dedup;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

public class RegenerateDedupKeysTest extends AbstractTest{

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/RegenerateDedupKeysTest.xml");
	}
	
	@Test
	public void simpleTest() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_REGEN_DEDUP_KEYS);
		JobParameters jobParams = new JobParameters();
		jobLauncher.run(job, jobParams);
		/*
		HarvestedRecord rec1 = harvestedRecordDao.get(21L);
		HarvestedRecord rec2 = harvestedRecordDao.get(22L);
		Assert.assertNotNull(rec1);
		Assert.assertNotNull(rec2);
		Assert.assertEquals(rec1.getPublicationYear(), new Long(1993));
		Assert.assertEquals(rec2.getPublicationYear(), new Long(1929));
		*/
	}
}
