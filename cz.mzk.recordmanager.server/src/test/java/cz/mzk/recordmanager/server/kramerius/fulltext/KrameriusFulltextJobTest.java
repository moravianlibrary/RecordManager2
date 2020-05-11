package cz.mzk.recordmanager.server.kramerius.fulltext;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.replay;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusFulltextJobTest extends AbstractTest {

	private static final String BASE_API_URL = "http://kramerius.mzk.cz/search/api/v5.0";

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private HttpClient httpClient;

	@BeforeMethod
	public void before() throws Exception {
		dbUnitHelper.init("dbunit/krameriusFulltextJobTest.xml");
	}

	@Test
	public void execute() throws Exception {
		mock();
		Job job = jobRegistry.getJob("krameriusFulltextJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("configurationId", new JobParameter(99001L));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
	}

	public void mock() throws Exception {
		reset(httpClient);
		InputStream children = this.getClass().getResourceAsStream("/sample/kramerius/children.json");
		InputStream ocr1 = getClass().getResourceAsStream("/sample/kramerius/ocr1.txt");
		InputStream ocr2 = getClass().getResourceAsStream("/sample/kramerius/ocr2.txt");
		expect(httpClient.executeGet(BASE_API_URL + "/item/uuid:039764f8-d6db-11e0-b2cd-0050569d679d/children")).andReturn(children);
		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f5a22336-2fd8-11e0-83a8-0050569d679d/streams/TEXT_OCR"), anyObject())).andReturn(ocr1);
		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f64abf47-2fd8-11e0-83a8-0050569d679d/streams/TEXT_OCR"), anyObject())).andReturn(ocr2);
		replay(httpClient);
	}

}
