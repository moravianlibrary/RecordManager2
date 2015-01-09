package cz.mzk.recordmanager.server.dedup;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;

public class DedupKeysGeneratorJobTest extends AbstractTest {
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;
	
	@Test
	public void execute() throws Exception {
		Job job = jobRegistry.getJob("dedupKeysGeneratorJob");
		JobParameters jobParams = new JobParameters();
		jobLauncher.run(job, jobParams);
	}

}
