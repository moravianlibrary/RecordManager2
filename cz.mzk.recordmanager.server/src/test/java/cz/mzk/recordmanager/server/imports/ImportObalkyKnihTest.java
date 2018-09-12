package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnotationDAO;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;
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
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class ImportObalkyKnihTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@Autowired
	private ObalkyKnihAnotationDAO obalkyKnihAnotationDAO;

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
		Assert.assertEquals(tocs.size(), 5);
		for (ObalkyKnihTOC toc : tocs) {
			Assert.assertNotNull(toc.getNbn());
			Assert.assertNotNull(toc.getIsbn());
		}
	}

	@Test
	public void anotations() throws Exception {
		String file = this.getClass().getResource("/import/obalkyknih/anotations.txt").getFile();

		Job job = jobRegistry.getJob(Constants.JOB_ID_IMPORT_ANOTATIONS);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_IN_FILE, new JobParameter(file));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		List<ObalkyKnihAnotation> results = obalkyKnihAnotationDAO.findAll();
		Assert.assertTrue(results.size() == 2);
		Assert.assertTrue(results.get(0).getIsbn().equals(9780545010221L)
				&& results.get(0).getNbn() == null
				&& results.get(0).getOclc() == null);
		Assert.assertTrue(results.get(1).getIsbn().equals(9788071820642L)
				&& results.get(1).getNbn().equals("cnb000604854")
				&& results.get(1).getOclc().equals("41216972"));

	}

}
