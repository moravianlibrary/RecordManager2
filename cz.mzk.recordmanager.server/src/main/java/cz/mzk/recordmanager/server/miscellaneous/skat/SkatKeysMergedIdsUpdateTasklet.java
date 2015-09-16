package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.UrlUtils;


/**
 * Tasklet communicates with Aleph on NKP using X-services 
 * and downloads identifiers of manually merged records from SKAT
 * @author mertam
 *
 */
public class SkatKeysMergedIdsUpdateTasklet implements Tasklet {

	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private static Logger logger = LoggerFactory.getLogger(SkatKeysMergedIdsUpdateTasklet.class);
	
	private static final Pattern BASE_RESPONSE_PATTERN = Pattern.compile(".*<set_number>(\\d+)</set_number>.*<no_records>(\\d+)</no_records>.*",Pattern.DOTALL);
	
	private static final Pattern DOWNLOAD_RESPONSE_PATTERN = Pattern.compile("<doc_number>(\\d+)</doc_number>",Pattern.DOTALL);
	
	private Date fromDate = null;
	
	private Set<String> downloadedKeys = new HashSet<>();
	
	public SkatKeysMergedIdsUpdateTasklet(Date fromDate) {
		if (fromDate.equals(new Date(0))) {
			this.fromDate = null;
		} else {
			this.fromDate = fromDate;
		}
		
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {

		long setNo = 0L;
		long recordsNo = 0L;
		
		String baseUrl = prepareAlephBaseUrl();			
		logger.info("Getting set info from Aleph: " + baseUrl);
		
		try (InputStream is = httpClient.executeGet(baseUrl)) {
			String rawResponse = IOUtils.toString(is);
			
			System.out.println(rawResponse);
			Matcher baseMatcher = BASE_RESPONSE_PATTERN.matcher(rawResponse);
			
			if (!baseMatcher.matches()) {
				logger.error("Response parsing failed, exiting...");
				return RepeatStatus.FINISHED;
			}
			
			setNo = Long.valueOf(baseMatcher.group(1));
			recordsNo = Long.valueOf(baseMatcher.group(2));
		} catch (IOException ioe) {
			logger.error("Response failed, exiting...: ");
			return RepeatStatus.FINISHED;
		};
		
		if (setNo == 0 || recordsNo == 0) {
			logger.info("Nothing to do, exiting...");
			return RepeatStatus.FINISHED;
		}
		
		downloadMergedSkatKeys(setNo, recordsNo);
		pushToDatabase();
		
		return RepeatStatus.FINISHED;
	}
	

	
	protected void downloadMergedSkatKeys(long setNo, long recordsNo) {
		long recordsPerResponse = 100;
		
		for (long offset = 0; offset < recordsNo; offset+= recordsPerResponse) {	
			String url = prepareAlephIncrementalUrl(setNo, recordsNo, offset, recordsPerResponse);
			logger.info("Downloading record from Aleph: " + url);
			
			try (InputStream is = httpClient.executeGet(url)) {
				String rawResponse = IOUtils.toString(is);
				Matcher matcher = DOWNLOAD_RESPONSE_PATTERN.matcher(rawResponse);
				while (matcher.find()) {
					downloadedKeys.add("SKC01-" + matcher.group(1));
				}
				
			} catch (IOException ioe) {
				logger.error("Download of records failed, giving up...");
			}
			
		}
	}
	
	protected void pushToDatabase() {
		List<String> currentBatch = new ArrayList<>();
		for (String currentKey: downloadedKeys) {
			currentBatch.add(currentKey);
			if (currentBatch.size() > 99) {
				push(currentBatch);
				currentBatch = new ArrayList<>();
			}
		}
		
		if (!currentBatch.isEmpty()) {
			push(currentBatch);
		}
	}
	
	protected void push(List<String> batch) {
		String query = "UPDATE skat_keys "
				+ "SET manually_merged = TRUE "
				+ "WHERE skat_record_id IN "
				+ "(SELECT id FROM harvested_record WHERE record_id in "
				+ "('" + String.join("','", batch) + "')"
				+ ")";
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(query).executeUpdate();
	}
	
	
	protected String prepareAlephBaseUrl() {
		String url = "http://aleph.nkp.cz/X";
		
		Map<String,String> params = new HashMap<>();
		params.put("op", "find");
		params.put("find_code", "wrd");
		params.put("base", "SKC");
		params.put("request", "ia=" + getAlephIaPrefix());
		
		return UrlUtils.buildUrl(url, params);
	}
	
	protected String prepareAlephIncrementalUrl(long setNo, long total, long offset, long countPerRequest) {
		String url = "http://aleph.nkp.cz/X";
		
		Map<String,String> params = new HashMap<>();
		params.put("op", "present");
		params.put("format", "marc");
		params.put("set_entry", prepareSetEntry(total, offset, countPerRequest));
		params.put("set_no", Long.toString(setNo));
		
		return UrlUtils.buildUrl(url, params);
	}
	
	protected static String prepareSetEntry(long total, long offset, long countPerRequest) {
		
		long min = offset;
		long max = total < offset + countPerRequest ? total : offset + countPerRequest;
		
		return String.format("%09d-%09d", min, max);
	}

	
	/**
	 * return 'ia' prefix obtained from 'fromDate'
	 * 
	 * @return
	 */
	protected String getAlephIaPrefix() {
		if (fromDate == null) {
			return "sl20*";
		}
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(fromDate);
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH) + 1;
	    return String.format("sl%04d%02d*",year,month);
	}
}
