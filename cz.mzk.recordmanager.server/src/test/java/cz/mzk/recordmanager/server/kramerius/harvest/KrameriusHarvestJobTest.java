package cz.mzk.recordmanager.server.kramerius.harvest;

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

import cz.mzk.recordmanager.server.util.Constants;

public class KrameriusHarvestJobTest extends AbstractKrameriusTest {

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}

	@Test
	public void execute() throws Exception {
		init();
		Job job = jobRegistry.getJob("krameriusHarvestJob");
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(99002L));
		JobParameters jobParams = new JobParameters(params);
		JobExecution execution = jobLauncher.run(job, jobParams);
		Assert.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
	}

}
