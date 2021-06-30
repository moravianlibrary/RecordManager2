package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import cz.mzk.recordmanager.server.oai.dao.KramDnntLabelDAO;
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

public class KramAvailabilityTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private KramAvailabilityDAO kramAvailabilityDAO;

	@Autowired
	private KramDnntLabelDAO kramDnntLabelDAO;

	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

	@Test
	public void metadataHarvestTest() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/import/kramAvailability/availability.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=100&start=0&wt=xml")).andReturn(response0);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=100&start=100&wt=xml")).andReturn(response1);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:1+document_type:monographunit&rows=100&start=0&wt=xml")).andReturn(response2);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(99001L));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertEquals(kramAvailabilityDAO.findAll().size(), 7);
		Assert.assertEquals(kramDnntLabelDAO.findAll().size(), 2);

	}

}
