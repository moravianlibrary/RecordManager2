package cz.mzk.recordmanager.server.index;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;

public class IndexRecordsToSolrJobTest extends AbstractTest {
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Test
	public void execute() throws Exception {
		Job job = jobRegistry.getJob("indexRecordsToSolrJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put("from", new JobParameter(new Date()));
		params.put("to", new JobParameter(new Date()));
		params.put("solrUrl", new JobParameter("http://localhost:8080/solr"));
		JobParameters jobParams = new JobParameters(params);
		jobLauncher.run(job, jobParams);
	}

}
