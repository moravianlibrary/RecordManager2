package cz.mzk.recordmanager.cmdline;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import cz.mzk.recordmanager.server.automatization.ScriptRunner;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.ResourceUtils;

public class CmdlineApplication {

	private static final String USAGE = ResourceUtils.asString("usage.txt");

	private static final String JOB_HELP = "help";
	private static final String JOB_RESTART = "restart";
	private static final String RUN_SCRIPT = "script";
	private static final String CLI_PARAM_JOB = "job";

	private static String jobName;

	public static void main(String[] args) throws Exception {
		JobParameters params = null;
		try {
			params = createJobParams(args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			jobName = JOB_HELP;
		}
		if (jobName.equals(JOB_HELP)) {
			usage();
		} else {
			run(params);
		}
	}

	private static void run(JobParameters jobParams) throws Exception {
		try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
			applicationContext
					.register(AppConfigCmdline.class);
			applicationContext.refresh();
			applicationContext.start();
			SessionFactory sessionFactory = applicationContext
					.getBean(SessionFactory.class);
			Session session = null;
			try {
				session = sessionFactory.openSession();
				TransactionSynchronizationManager.bindResource(sessionFactory,
						new SessionHolder(session));
				performJob(applicationContext, jobParams);
			} finally {
				SessionFactoryUtils.closeSession(session);
			}
		}
	}

	private static void performJob(AnnotationConfigApplicationContext applicationContext, final JobParameters jobParams)
			throws Exception {
		if (jobName.equals(JOB_RESTART)) {
			BatchService batchService = applicationContext.getBean(BatchService.class);
			if (jobParams.getParameters().containsKey("jobExecutionId")) {
				Long jobExecutionId = Long.valueOf(jobParams.getString("jobExecutionId"));
				BatchJobExecutionDTO jobExecution = batchService.getJobExecution(jobExecutionId);
				batchService.restart(jobExecution);
			} else {
				for (BatchJobExecutionDTO jobExecution : batchService.getRunningJobExecutions()) {
					Runnable runnable = () -> batchService.restart(jobExecution);
						executeInThread(runnable);
				}
			}
		} else if (jobName.equals(RUN_SCRIPT)) {
			ScriptRunner scriptRunner = applicationContext.getBean(ScriptRunner.class);
			if (!jobParams.getParameters().containsKey("scriptname")) {
				throw new IllegalArgumentException("Parameter scriptname is missing");
			}
			String scriptPath = jobParams.getString("scriptname");
			scriptRunner.run(scriptPath);
		} else {
			JobExecutor executor = applicationContext.getBean(JobExecutor.class);
			Runnable runnable = () -> executor.execute(jobName, jobParams, true);
			executeInThread(runnable);
		}
	}

	private static void executeInThread(Runnable runnable) throws InterruptedException {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.join();
	}

	private static void usage() {
		System.out.println(USAGE);
	}

	/**
	 * create {@link JobParameters} object from command line arguments
	 * 
	 * @param args
	 * @return
	 */
	private static JobParameters createJobParams(String[] args) {
		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
		Iterator<String> iter = Arrays.asList(args).iterator();
		int index = 0;
		while (iter.hasNext()) {
			index++;
			String paramName = iter.next();
			if (!paramName.startsWith("-")) {
				throw new IllegalArgumentException(
						String.format("Expected parameter name at place of %s", index));
			}
			paramName = paramName.substring(1);
			if (!iter.hasNext()) {
				throw new IllegalArgumentException(String.format("Parameter \"%s\" has no value.", paramName));
			}
			String paramValue = iter.next();
			if (paramName.equals(CLI_PARAM_JOB)) {
				jobName = paramValue;
			} else {
				params.put(paramName, new JobParameter(paramValue));
			}
		}
		if (jobName == null) {
			throw new IllegalArgumentException("Missing job name.");
		}
		return new JobParameters(params);
	}

}
