package cz.mzk.recordmanager.cmdline;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

import cz.mzk.recordmanager.server.util.Constants;

public class CmdlineApplication {

	private static final String JOB_HARVEST	= "harvest";
	private static final String JOB_HELP	= "help";
	
	private static final String CLI_PARAM_JOB				= "job";
	private static final String CLI_PARAM_HARVEST_CONFIG_ID = "configID";
	private static final String CLI_PARAM_FROM_TIMESTAMP	= "from";
	private static final String CLI_PARAM_UNTIL_TIMESTAMP	= "until";
	
	public static void main(String[] args) throws Exception {
		
		final Options options = generateOptions();
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmdLine = parser.parse(options, args);

		if (cmdLine == null) {
			throw new ParseException("Command line arguments parsing failed");
		}
		
		final String job = cmdLine.getOptionValue(CLI_PARAM_JOB);
		if (job == null) {
			printHelp(options);
			throw new IllegalArgumentException("No job specified");
		}
		
		switch(job) {
			case JOB_HARVEST: 
				Long numConfID = processConfigID(cmdLine.getOptionValue(CLI_PARAM_HARVEST_CONFIG_ID));
				if (numConfID == null) {
					return;
				}
				Date sdfFrom = processDateTime(cmdLine.getOptionValue(CLI_PARAM_FROM_TIMESTAMP));
				Date sdfUntil = processDateTime(cmdLine.getOptionValue(CLI_PARAM_UNTIL_TIMESTAMP));
				harvest(numConfID, sdfFrom, sdfUntil); 
				break; 
			
			case JOB_HELP:  
				/* FALLTHROUGH */
			default: printHelp(options);
		}
		
	}
	
	/**
	 * Perform harvest job
	 * @param confID identifier of oai_harvest_conf
	 * @param dateFrom timestamp
	 * @param dateUntil timestamp
	 * @throws Exception
	 */
	private static void harvest(Long confID, Date dateFrom, Date dateUntil) throws Exception {
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
				Job job = jobRegistry.getJob(Constants.JOB_ID_HARVEST);
				Map<String, JobParameter> params = new HashMap<String, JobParameter>();
				params.put(Constants.JOB_PARAM_CONF_ID, new JobParameter(confID));
				if (dateFrom != null) {
					params.put(Constants.JOB_PARAM_FROM_DATE, new JobParameter(dateFrom));
				}
				if (dateUntil != null) {
					params.put(Constants.JOB_PARAM_UNTIL_DATE, new JobParameter(dateFrom));
				}
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
	
	/**
	 * generate Options for command line interface
	 * @return {@link Options}
	 */
	private static Options generateOptions() {
		Option optJob = new Option( "j", CLI_PARAM_JOB, true, "job type (harvest|help)");
		optJob.setRequired(true);
		
		Options options = new Options();
		options.addOption(optJob);
		options.addOption( "i", CLI_PARAM_HARVEST_CONFIG_ID, true, "numeric identifier of configuration" );
		options.addOption( "f", CLI_PARAM_FROM_TIMESTAMP, true, "Timestamp of begin (format: YYYY-MM-DDThh:mm:ssZ)" );
		options.addOption( "u", CLI_PARAM_UNTIL_TIMESTAMP, true, "Timestamp of end (format: YYYY-MM-DDThh:mm:ssZ)" );
		
		return options;
	}
	
	/**
	 * @param strConfID
	 * @return Long or null if error occurs
	 */
	private static Long processConfigID(final String strConfID) {
		if (strConfID == null) {
			System.err.println("Missing harvest configuration ID");
			return null;
		}
		
		Long numConfID = null;
		try {
			 numConfID = new Long(strConfID);
		} catch (NumberFormatException e) {
			System.err.println("Invalid number format: " + strConfID);
			return null;
		}
		return numConfID;
	}
	
	/**
	 * creates SimpleDateFormat in format "YYYY-MM-DDThh:mm:ssZ" from given string
	 * @param strDate
	 * @return {@link SimpleDateFormat} or null if error occurs
	 * @throws java.text.ParseException 
	 */
	private static Date processDateTime(final String strDate) throws java.text.ParseException {
		if (strDate == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return sdf.parse(strDate);
	}
	
	private static void printHelp(final Options options) {
		String header = "Recordmanager2 command line interface.\n\n";
		HelpFormatter formatter = new HelpFormatter();
		
		formatter.setOptionComparator(new Comparator<Option>() {

			private static final String PREFERED_ORDER = "jifu";
			
			@Override
			public int compare(Option o1, Option o2) {
				return PREFERED_ORDER.indexOf(o1.getOpt()) - PREFERED_ORDER.indexOf(o2.getOpt());
			}
			
		});
		
		formatter.printHelp("java -Dlogback.configurationFile=logback.xml -DCONFIG_DIR=. " 
				+ "-jar target/cz.mzk.recordmanager.cmdline-1.0.0-SNAPSHOT.jar", header, options, "", true);
		
	}
	
}
