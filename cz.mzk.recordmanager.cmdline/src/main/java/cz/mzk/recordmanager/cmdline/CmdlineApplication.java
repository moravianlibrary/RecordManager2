package cz.mzk.recordmanager.cmdline;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class CmdlineApplication {

	public static void main(String[] args) throws Exception {
		try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
			applicationContext.register(AppConfigCmdline.class);
			applicationContext.refresh();
			applicationContext.start();
			SessionFactory sessionFactory = applicationContext
					.getBean(SessionFactory.class);
			Session session = null;
			try {
				session = sessionFactory.openSession();
				TransactionSynchronizationManager.bindResource(sessionFactory,
						new SessionHolder(session));
				JobRegistry jobRegistry = applicationContext
						.getBean(JobRegistry.class);
				JobLauncher jobLauncher = applicationContext.getBean(
						"jobLauncher", JobLauncher.class);
				Job job = jobRegistry.getJob("oaiHarvestJob");
				Map<String, JobParameter> params = new HashMap<String, JobParameter>();
				params.put("configurationId", new JobParameter(300L));
				JobParameters jobParams = new JobParameters(params);
				jobLauncher.run(job, jobParams);
				SessionHolder holder = (SessionHolder) TransactionSynchronizationManager
						.getResource(sessionFactory);
				session = holder.getSession();
				session.flush();
				TransactionSynchronizationManager
						.unbindResource(sessionFactory);
			} finally {
				SessionFactoryUtils.closeSession(session);
			}
		}
	}
}
