package cz.mzk.recordmanager.server.miscellaneous;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.oai.dao.ZiskejLibraryDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class ZiskejLibrariesTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ZiskejLibraryDAO ziskejLibraryDAO;

	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

//	@Test
	public void harvestLibrariesListTest() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/import/ziskej/Ziskej.json");
		expect(httpClient.executeGet("https://ziskej.techlib.cz:9080/api/v1/libraries")).andReturn(response0);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_ZISKEJ_LIBRARIES);
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertEquals(18, ziskejLibraryDAO.findAll().size());
	}

}
