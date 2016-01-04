package cz.mzk.recordmanager.server.imports;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.InputStream;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;

public class ImportObalkyKnihTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/IndexRecordsToSolrJobTest.xml");
	}

	@Test
	public void run() throws Exception {
		reset(httpClient);
		InputStream response = this.getClass().getResourceAsStream("/obalkyknih/toc.xml");
		expect(httpClient.executeGet("http://www.obalkyknih.cz/api/toc.xml")).andReturn(response);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_OBALKY_KNIH);
		JobParameters jobParams = new JobParameters();
		jobLauncher.run(job, jobParams);

		List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.findAll();
		Assert.assertEquals(tocs.size(), 3);
		for (ObalkyKnihTOC toc : tocs) {
			Assert.assertNotNull(toc.getNbn());
		}
	}

}
