package cz.mzk.recordmanager.server.oai.harvest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:appCtx-recordmanager-server-test.xml" })
@Import({OAIHarvestJob.class})
public class OAIHarvestJobTest extends AbstractTestNGSpringContextTests {
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobRegistry jobRegistry;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Test
	public void execute() throws Exception {
		Job job = jobRegistry.getJob("oaiHarvestJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("url", new JobParameter("http://aleph.mzk.cz/OAI"));
		params.put("metadataPrefix", new JobParameter("marc21"));
		//params.put("set", new JobParameter("marc21"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}
	
}
