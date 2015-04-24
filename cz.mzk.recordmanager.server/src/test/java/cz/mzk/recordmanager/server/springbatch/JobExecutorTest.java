package cz.mzk.recordmanager.server.springbatch;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.springbatch.JobParameterDeclaration;

public class JobExecutorTest extends AbstractTest {
	
	@Autowired
	private JobExecutor jobExecutor;
	
	@Test
	public void getJobNames() {
		Collection<String> jobNames = jobExecutor.getJobNames();
		Assert.assertTrue(jobNames.size() >= 2);
	}
	
	@Test
	public void getParametersOfJob() {
		Collection<JobParameterDeclaration> params = jobExecutor.getParametersOfJob("oaiHarvestJob");
		Assert.assertEquals(params.size(), 4);
	}

}
