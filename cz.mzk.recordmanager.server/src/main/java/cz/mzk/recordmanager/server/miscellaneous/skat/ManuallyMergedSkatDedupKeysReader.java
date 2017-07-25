package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.UrlUtils;

public class ManuallyMergedSkatDedupKeysReader implements ItemReader<Set<SkatKey>> {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private SkatKeyDAO skatKeyDao;
	
	@Autowired
	private HarvestedRecordDAO hrDao;
	
	private static Logger logger = LoggerFactory
			.getLogger(ManuallyMergedSkatDedupKeysReader.class);

	private static final Pattern PATTERN = Pattern
			.compile("http://aleph.nkp.cz/F/([A-Z0-9-]*)\\?func=short-mail-0");
	private static final Pattern PATTERN2 = Pattern
			.compile("(http://aleph.nkp.cz/exlibris/aleph/a22_1/tmp/[^\\.]*\\.sav)");

	private Date fromDate = null;
	private Date toDate = null;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private Set<String> downloadedKeys = new HashSet<>();
	private Map<String, String> headers;

	public ManuallyMergedSkatDedupKeysReader(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.headers = Collections
				.singletonMap("User-Agent",
						"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:48.0) Gecko/20100101 Firefox/48.0");
	}

	@Override
	public Set<SkatKey> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		Matcher matcher;
		Date date = fromDate;

		if (toDate == null) {
			toDate = new Date();
		}
		toDate = DateUtils.truncate(toDate, Calendar.DAY_OF_MONTH);
		while (date.before(toDate)) {
			downloadedKeys.clear();
			String get = IOUtils.toString(harvest(prepareAlephBaseUrl(DATE_FORMAT.format(date))));

			if (!(matcher = PATTERN.matcher(get)).find()) {
				logger.info("Session not found!!!");
			} else {
				sleep(20000, 30000); // wait 20 - 30 seconds
				get = IOUtils.toString(harvest(prepareAlephMailUrl(matcher.group(1))));
				if (!(matcher = PATTERN2.matcher(get)).find()) {
					logger.info("File with results not found!!!");
				} else {
					sleep(20000, 30000); // wait 20 - 30 seconds
					SkatIdsStreamReader skatIdsStreamReader = new SkatIdsStreamReader(harvest(matcher.group(1)));
					while (skatIdsStreamReader.hasNext()) {
						String id = skatIdsStreamReader.next();
						if (id != null) {
							downloadedKeys.add(id);
						}
					}
				}
			}
			date = DateUtils.addDays(date, 1); // next day
			if (date.before(toDate)) sleep(120000, 180000); // wait 2-3 minutes
		}
		Set<SkatKey> results = new HashSet<>();
		downloadedKeys.forEach(key -> {
			HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(key, Constants.IMPORT_CONF_ID_CASLIN);
			if (hr != null) {
				List<SkatKey> skatkeyList = skatKeyDao.getSkatKeysForRecord(hr.getId());
				if (skatkeyList != null) results.addAll(skatkeyList);
			}
		});

		return results;
	}

	protected void sleep(int min, int max) {
		try {
			int time = min + (int) (Math.random() * ((max - min) + 1));
			logger.info("Waiting: " + time / 1000 + " seconds");
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected InputStream harvest(String url) throws IOException {
		logger.info("Harvesting from: " + url);
		return httpClient.executeGet(url, headers);
	}

	protected void pushToDatabase() {
		for (String currentKey : downloadedKeys) {
			push(currentKey);
		}
		downloadedKeys.clear();
	}

	protected void push(String recordId) {
		String query = "UPDATE skat_keys "
				+ "SET manually_merged = TRUE "
				+ "WHERE skat_record_id IN "
				+ "(SELECT id FROM harvested_record WHERE import_conf_id = ? AND record_id = ?"
				+ ")";
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(query)
				.setLong(0, Constants.IMPORT_CONF_ID_CASLIN)
				.setString(1, recordId).executeUpdate();

		query = "UPDATE harvested_record SET next_dedup_flag=true WHERE import_conf_id = ? AND record_id = ?";
		session.createSQLQuery(query)
				.setLong(0, Constants.IMPORT_CONF_ID_CASLIN)
				.setString(1, recordId).executeUpdate();
	}

	protected String prepareAlephBaseUrl(String date) {
		String url = "http://aleph.nkp.cz/F/";

		Map<String, String> params = new HashMap<>();
		params.put("func", "find-c");
		params.put("ccl_term", "ia=sl" + date + "*");
		params.put("adjacent", "N");
		params.put("local_base", "SKC");

		return UrlUtils.buildUrl(url, params);
	}

	protected String prepareAlephMailUrl(String context) {
		String url = "http://aleph.nkp.cz/F/" + context;

		Map<String, String> params = new HashMap<>();
		params.put("func", "short-mail");
		params.put("records", "ALL");
		params.put("format", "999");
		params.put("own_format", "SYS");
		params.put("encoding", "UTF_TO_WEB_MAIL_ASCI");
		params.put("EMAIL", "");
		params.put("SUBJECT", "");
		params.put("x", "32");
		params.put("y", "11");

		return UrlUtils.buildUrl(url, params);
	}

}
