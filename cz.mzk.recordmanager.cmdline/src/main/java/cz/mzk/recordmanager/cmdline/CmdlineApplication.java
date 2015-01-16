package cz.mzk.recordmanager.cmdline;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CmdlineApplication {

	public static void main(String[] args) throws Exception {
		try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
			applicationContext.register(AppConfigCmdline.class);
			applicationContext.refresh();
			applicationContext.start();
			JobRegistry jobRegistry = applicationContext
					.getBean(JobRegistry.class);
			JobLauncher jobLauncher = applicationContext.getBean("jobLauncher",
					JobLauncher.class);
			Job job = jobRegistry.getJob("oaiHarvestJob");
			Map<String, JobParameter> params = new HashMap<String, JobParameter>();
			params.put("configurationId", new JobParameter(300L));
			JobParameters jobParams = new JobParameters(params);
			jobLauncher.run(job, jobParams);

		}
	}
}
