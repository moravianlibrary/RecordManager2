package cz.mzk.recordmanager.server.miscellaneous.caslin.keys;

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

import cz.mzk.recordmanager.server.util.Constants;
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
		this.fromDate = fromDate;
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		for (String basePrefix: preparePrefixes()) {
			
			long setNo = 0L;
			long recordsNo = 0L;
			
			String baseUrl = prepareAlephBaseUrl(basePrefix);			
			logger.info("Getting set info from Aleph: " + baseUrl);
			
			try (InputStream is = httpClient.executeGet(baseUrl)) {
				String rawResponse = IOUtils.toString(is);
				Matcher baseMatcher = BASE_RESPONSE_PATTERN.matcher(rawResponse);
				
				if (!baseMatcher.matches()) {
					logger.error("No usable data in response...");
					continue;
				}
				
				setNo = Long.valueOf(baseMatcher.group(1));
				recordsNo = Long.valueOf(baseMatcher.group(2));
			} catch (IOException ioe) {
				logger.error("Response failed...");
				continue;
			}
			
			if (setNo == 0 || recordsNo == 0) {
				logger.info("Nothing to do...");
			}
			
			downloadMergedSkatKeys(setNo, recordsNo);
			pushToDatabase();
		}

		
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
		for (String currentKey: downloadedKeys) {
			push(currentKey);
		}
		downloadedKeys.clear();
	}
	
	protected void push(String recordId) {
		String query = "UPDATE skat_keys "
				+ "SET manually_merged = TRUE "
				+ "WHERE skat_record_id IN "
				+ "(SELECT id FROM harvested_record WHERE import_conf_id = ? AND record_id = ?"
				+ ')';
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(query)
			.setLong(0, Constants.IMPORT_CONF_ID_CASLIN)
			.setString(1, recordId)
			.executeUpdate();

		query = "UPDATE harvested_record SET next_dedup_flag=TRUE WHERE import_conf_id = ? AND record_id = ?";
		session.createSQLQuery(query)
			.setLong(0, Constants.IMPORT_CONF_ID_CASLIN)
			.setString(1, recordId)
			.executeUpdate();
	}
	
	/**
	 * return string representation of all ia prefixes from 'fromDate'
	 *  up to current month.
	 * @return string representation of all ia prefixes
	 *
	 */
	protected List<String> preparePrefixes() {
		List<String> result = new ArrayList<>();
		
		//setup lower and upper bound
		Calendar currentCal = Calendar.getInstance();
		int currentYear = currentCal.get(Calendar.YEAR);
		int currentMonth = currentCal.get(Calendar.MONTH) + 1;
		
		int startYear = 2000;
		int startMonth = 1;
		if (fromDate != null) {
			Calendar fromCal = Calendar.getInstance();
			fromCal.setTime(fromDate);
			
			// detect incremental update and process with finer granularity
			int countOfDays = daysBetween(fromCal, Calendar.getInstance()); 
			if (countOfDays < 7) {
				return prepreIncrementalPrefixes(fromCal, countOfDays);
			}
			
			startYear = fromCal.get(Calendar.YEAR);
			startMonth = fromCal.get(Calendar.MONTH) + 1;
		}
		
		for (int year = startYear; year <= currentYear; year++) {
			for (int month = 1; month <= 12; month++) {
				if (year == currentYear && month > currentMonth) {
					continue;
				}
				if (year == startYear && month < startMonth) {
					continue;
				}
				// process full month
				for (int partOfMonth = 0; partOfMonth <= 3; partOfMonth++) {
					result.add(String.format("sl%d%02d%d*", year, month, partOfMonth));
				}
			}
		}
		return result;
	}
	
	protected List<String> prepreIncrementalPrefixes(Calendar fromCal, int countOfDays) {
		List<String> result = new ArrayList<>();
		
		Calendar local = (Calendar) fromCal.clone();
		
		for (int i = 1; i <= countOfDays; i++) {
			local.add(Calendar.DAY_OF_YEAR, 1);
			result.add(String.format("sl%d%02d%02d*", local.get(Calendar.YEAR), local.get(Calendar.MONTH) +1, local.get(Calendar.DAY_OF_MONTH)));
		}
		return result;
	}
	
	protected String prepareAlephBaseUrl(String iaPrefix) {
		String url = "http://aleph.nkp.cz/X";
		
		Map<String,String> params = new HashMap<>();
		params.put("op", "find");
		params.put("find_code", "wrd");
		params.put("base", "SKC");
		params.put("request", "ia=" + iaPrefix);
		
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
	
	protected String prepareSetEntry(long total, long offset, long countPerRequest) {
		
		long min = offset;
		long max = total < offset + countPerRequest ? total : offset + countPerRequest;
		
		return String.format("%09d-%09d", min, max);
	}
	
	protected int daysBetween(Calendar day1, Calendar day2){
	    Calendar dayOne = (Calendar) day1.clone(),
	            dayTwo = (Calendar) day2.clone();

	    if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
	        return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
	    } else {
	        if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
	            //swap them
	            Calendar temp = dayOne;
	            dayOne = dayTwo;
	            dayTwo = temp;
	        }
	        int extraDays = 0;

	        int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

	        while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
	            dayOne.add(Calendar.YEAR, -1);
	            // getActualMaximum() important for leap years
	            extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
	        }

	        return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays ;
	    }
	}
}
