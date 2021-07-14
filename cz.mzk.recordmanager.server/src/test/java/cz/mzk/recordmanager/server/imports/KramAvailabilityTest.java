package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.KramAvailability;
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

	private static final int ROWS = 5000;

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
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=" + ROWS + "&start=0&wt=xml")).andReturn(response0);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=" + ROWS + "&start=" + ROWS + "&wt=xml")).andReturn(response1);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:1+document_type:monographunit&rows=" + ROWS + "&start=0&wt=xml")).andReturn(response2);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(99001L));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		Assert.assertEquals(kramAvailabilityDAO.findAll().size(), 7);
		Assert.assertEquals(kramDnntLabelDAO.findAll().size(), 2);
	}

	@Test
	public void metadataHarvestPageTest() throws Exception {
		reset(httpClient);
		InputStream response0 = this.getClass().getResourceAsStream("/import/kramAvailability/volume.xml");
		InputStream response1 = this.getClass().getResourceAsStream("/import/kramAvailability/issue.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/import/kramAvailability/page.xml");
		InputStream empty0 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		InputStream empty1 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		InputStream empty2 = this.getClass().getResourceAsStream("/import/kramAvailability/availabilityEmpty.xml");
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type&q=fedora.model:periodicalvolume&rows=" + ROWS + "&start=0&wt=xml")).andReturn(response0);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type&q=fedora.model:periodicalvolume&rows=" + ROWS + "&start=" + ROWS + "&wt=xml")).andReturn(empty0);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,rok,issn&q=fedora.model:periodicalitem&rows=" + ROWS + "&start=0&wt=xml")).andReturn(response1);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,rok,issn&q=fedora.model:periodicalitem&rows=" + ROWS + "&start=" + ROWS + "&wt=xml")).andReturn(empty1);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,title,rok,rels_ext_index&q=fedora.model:page+model_path:*periodicalitem/page&rows=" + ROWS + "&start=0&wt=xml")).andReturn(response2);
		expect(httpClient.executeGet("https://kramerius.mzk.cz/search/api/v5.0/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,title,rok,rels_ext_index&q=fedora.model:page+model_path:*periodicalitem/page&rows=" + ROWS + "&start=" + ROWS + "&wt=xml")).andReturn(empty2);
		replay(httpClient);

		Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST_KRAM_AVAILABILITY);
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(99001L));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);

		KramAvailability availability = kramAvailabilityDAO.getByUuid("uuid:01bedf30-513a-11e9-918e-5ef3fc9ae867").get(0);
		Assert.assertNotNull(availability);
		Assert.assertNotNull(availability.getIssn());
		Assert.assertNotNull(availability.getIssue());
		Assert.assertNotNull(availability.getPage());
		Assert.assertNotNull(availability.getVolume());
		Assert.assertNotNull(availability.getYaer());
	}

}
