package cz.mzk.recordmanager.server.oai.harvest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.util.HttpClient;

public class OAIHarvestJobTest extends AbstractTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private HttpClient httpClient;
	
	@Test
	public void execute() throws Exception {
		InputStream response = this.getClass().getResourceAsStream("/sample/ListRecords.xml");
		expect(httpClient.executeGet("http://aleph.mzk.cz/OAI?verb=ListRecords&metadataPrefix=marc21")).andReturn(response);
		replay(httpClient);
		Job job = jobRegistry.getJob("oaiHarvestJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("configurationId", new JobParameter(300L));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

}
