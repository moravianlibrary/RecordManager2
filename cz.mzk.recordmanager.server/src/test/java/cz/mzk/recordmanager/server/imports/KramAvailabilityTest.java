package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.imports.kramAvailability.KramAvailabilityReader;
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
		int rows = KramAvailabilityReader.ROWS;
		reset(httpClient);
		InputStream response1 = this.getClass().getResourceAsStream("/sample/kramerius/info-v5.json");
		InputStream response2 = this.getClass().getResourceAsStream("/import/kramAvailability/availability.xml");
		InputStream response3 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		InputStream response4 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/info")).andReturn(response1);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=" + rows + "&start=0&wt=xml&sort=PID+asc")).andReturn(response2);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=" + rows + "&start=" + rows + "&wt=xml&sort=PID+asc")).andReturn(response3);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:1+document_type:monographunit&rows=1000&start=0&wt=xml&sort=PID+asc")).andReturn(response4);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(99001L));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertEquals(kramAvailabilityDAO.findAll().size(), 7);
		Assert.assertEquals(kramDnntLabelDAO.findAll().size(), 6);

	}

}
