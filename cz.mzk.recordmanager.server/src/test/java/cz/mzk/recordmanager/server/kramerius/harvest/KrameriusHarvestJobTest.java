package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.springframework.batch.core.*;
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

public class KrameriusHarvestJobTest extends AbstractKrameriusTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HttpClient httpClient;

	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}

	@Test
	public void execute() throws Exception {
		reset(httpClient);
		InputStream response1 = this.getClass().getResourceAsStream("/sample/kramerius/info-v5.json");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/kramerius/DownloadItem1.xml");
		InputStream response3 = this.getClass().getResourceAsStream("/sample/kramerius/DownloadItem2.xml");
		expect(httpClient.executeGet("http://k4.techlib.cz/search/api/v5.0/info")).andReturn(response1);
		expect(httpClient.executeGet("http://k4.techlib.cz/search/api/v5.0/item/uuid:00931210-02b6-11e5-b939-0800200c9a66/streams/DC")).andReturn(response2);
		expect(httpClient.executeGet("http://k4.techlib.cz/search/api/v5.0/item/uuid:0095bca0-614f-11e2-bcfd-0800200c9a66/streams/DC")).andReturn(response3);
		replay(httpClient);

		initMockedSolrServer();

		Job job = jobRegistry.getJob("krameriusHarvestJob");
		Map<String, JobParameter> params = new HashMap<>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(99002L));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
	}

}
