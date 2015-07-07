package cz.mzk.recordmanager.cmdline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import cz.mzk.recordmanager.server.AppConfig;
import cz.mzk.recordmanager.server.springbatch.JobExecutor;
import cz.mzk.recordmanager.server.util.Constants;

public class CmdlineApplication {

	private static final String JOB_HELP = "help";
	private static final String JOB_RESTART = "restart";
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
			printHelp();
		} else {
			performJob(params);
		}
	}

	private static void performJob(final JobParameters jobParams)
			throws Exception {
		try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
			applicationContext
					.register(AppConfigCmdline.class, AppConfig.class);
			applicationContext.refresh();
			applicationContext.start();
			SessionFactory sessionFactory = applicationContext
					.getBean(SessionFactory.class);
			Session session = null;
			try {
				session = sessionFactory.openSession();
				TransactionSynchronizationManager.bindResource(sessionFactory,
						new SessionHolder(session));
				if (jobName.equals(JOB_RESTART)) {
					BatchService batchService = applicationContext
							.getBean(BatchService.class);
					if (jobParams.getParameters().containsKey("jobExecutionId")) {
						Long jobExecutionId = Long.valueOf(jobParams.getString("jobExecutionId"));
						BatchJobExecutionDTO jobExecution = batchService.getJobExecution(jobExecutionId);
						batchService.restart(jobExecution);
					} else {
						for (BatchJobExecutionDTO jobExecution : batchService
								.getRunningJobExecutions()) {
							Runnable runnable = () -> batchService.restart(jobExecution);
							executeInThread(runnable);
						}
					}
				} else {
					JobExecutor executor = applicationContext
							.getBean(JobExecutor.class);
					Runnable runnable = () -> executor.execute(jobName,
							jobParams, true);
					executeInThread(runnable);
				}
			} finally {
				SessionFactoryUtils.closeSession(session);
			}
		}
	}

	private static void executeInThread(Runnable runnable) throws InterruptedException {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.join();
	}

	private static void printHelp() {
		System.out.println("Recordmanager2 command line interface.\n");
		System.out
				.println("USAGE:\njava -Dlogback.configurationFile=logback.xml -DCONFIG_DIR=. "
						+ "-jar target/cz.mzk.recordmanager.cmdline-1.0.0-SNAPSHOT.jar -param1 value1 -param2 value2\n");

		System.out.println("Parameters:\n");
		
		
		System.out.println(String.format(
				"%-10sname of a job. Available:", CLI_PARAM_JOB));
		for (String job: getJobList()) {
			System.out.println(String.format("\t\t%s", job));
		}
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_CONF_ID, "identifier of a job (LONG)"));
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_FROM_DATE, "from date (DATE)"));
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_UNTIL_DATE, "until date (DATE)"));
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_SOLR_URL, "URL of solr instance (String)"));
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_FORMAT, "Metadata format (String). Available: line|aleph|iso|xml"));
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_OUT_FILE, "Target file used for export (String)."));
		System.out.println(String.format("%-10s%s",
				Constants.JOB_PARAM_IN_FILE, "Target file used for import (String)."));
	}
	
	private static List<String> getJobList() {
		List<String> jobList = new ArrayList<>();
		jobList.add(Constants.JOB_ID_HARVEST);
		jobList.add(Constants.JOB_ID_HARVEST_PART);
		jobList.add(Constants.JOB_ID_DEDUP);
		jobList.add(Constants.JOB_ID_SOLR_INDEX);
		jobList.add(Constants.JOB_ID_SOLR_INDEX_ALL_RECORDS);
		jobList.add(Constants.JOB_ID_REGEN_DEDUP_KEYS);
		jobList.add(Constants.JOB_ID_EXPORT);
		jobList.add(Constants.JOB_ID_IMPORT);
		jobList.add(JOB_HELP);
		return jobList;
	}

	/**
	 * create {@link JobParameters} object from command line arguments
	 * 
	 * @param args
	 * @return
	 */
	private static JobParameters createJobParams(String[] args) {
		Map<String, JobParameter> pMap = new HashMap<String, JobParameter>();
		String paramName = null;
		for (int i = 0; i < args.length; i++) {
			if (i % 2 == 0) {
				// expected parameter name
				if (args[i].startsWith("-")) {
					paramName = args[i].substring(1);
				} else {
					throw new IllegalArgumentException(
							"Expected parameter name at place of " + args[i]);
				}
			} else {
				// parameter value
				if (args[i].startsWith("-")) {
					throw new IllegalArgumentException("Paramenter \""
							+ paramName + "\" has no value.");
				}
				if (paramName.equals(CLI_PARAM_JOB)) {
					jobName = args[i];
				} else {
					pMap.put(paramName, new JobParameter(args[i]));
				}
				paramName = null;
			}
		}

		if (paramName != null) {
			throw new IllegalArgumentException("Paramenter \"" + paramName
					+ "\" has no value.");
		}

		if (jobName == null) {
			throw new IllegalArgumentException("Missing job name.");
		}
		return new JobParameters(pMap);
	}

}
