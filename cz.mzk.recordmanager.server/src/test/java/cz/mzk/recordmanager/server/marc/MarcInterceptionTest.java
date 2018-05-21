package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.DBUnitHelper;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class MarcInterceptionTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private DBUnitHelper dbUnitHelper;

	private String testFileLine1;

	@BeforeClass
	public void init() {
		testFileLine1 = this.getClass().getResource("/import/marcline/MZK01-000000116.mrc").getFile();
	}

	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

	@Test
	public void interceptionTest() throws Exception {
		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(300L));
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(testFileLine1));
		params.put(Constants.JOB_PARAM_FORMAT, new JobParameter("line"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		HarvestedRecord insertedRecord = harvestedRecordDao.findByIdAndHarvestConfiguration("000000116", 300L);
		Assert.assertNotNull(insertedRecord);
		MarcRecord marcRecord = marcXmlParser.parseRecord(insertedRecord);
		Assert.assertEquals(marcRecord.getDataFields("996").get(0).getSubfield('t').getData(), "BOA001.000000116.MZK50000000116000010");
	}
}
