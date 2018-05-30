package cz.mzk.recordmanager.server.miscellaneous.skat;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;
import cz.mzk.recordmanager.server.util.ApacheHttpClient;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.UrlUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManuallyMergedSkatDedupKeysReader implements ItemReader<Set<SkatKey>> {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private SkatKeyDAO skatKeyDao;

	@Autowired
	private HarvestedRecordDAO hrDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(ManuallyMergedSkatDedupKeysReader.class);

	private static final String SKC_ID_PREFIX = "SKC01-";
	private static final String ALEPH_URL_QUERY = "https://aleph.nkp.cz/F/";
	private static final String ALEPH_URL_REGEX = "http[s]://aleph.nkp.cz/";

	private static final Pattern CONTEXT_ID = Pattern
			.compile(ALEPH_URL_REGEX + "F/([A-Z0-9-]*)\\?func=short-mail-0");
	private static final Pattern FILE_URL = Pattern
			.compile('(' + ALEPH_URL_REGEX + "exlibris/aleph/a22_1/tmp/[^.]*\\.sav)");
	private static final Pattern SYSNO = Pattern.compile("System.cislo\\s*([0-9]+)");

	private static final Map<String, String> ALEPH_MAIL_PARAMS = createAlephMailParams();
	private static final Map<String, String> HEADERS = Collections.singletonMap("User-Agent",
			"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:48.0) Gecko/20100101 Firefox/48.0");

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private Date toDate = null;
	private Date counterDate = null;
	private Set<String> downloadedKeys = new HashSet<>();

	public ManuallyMergedSkatDedupKeysReader(Date fromDate, Date toDate) {
		this.counterDate = fromDate;
		this.toDate = toDate;
	}

	@Override
	public Set<SkatKey> read() throws Exception {
		Matcher matcher;

		if (toDate == null) {
			toDate = new Date();
		}
		toDate = DateUtils.truncate(toDate, Calendar.DAY_OF_MONTH);
		if (!counterDate.after(toDate)) {
			downloadedKeys.clear();
			String get = IOUtils.toString(harvest(prepareAlephBaseUrl(DATE_FORMAT.format(counterDate))));

			if (!(matcher = CONTEXT_ID.matcher(get)).find()) {
				LOGGER.info("Session not found!!!");
			} else {
				sleep(20000, 30000); // wait 20 - 30 seconds
				get = IOUtils.toString(harvest(prepareAlephMailUrl(matcher.group(1))));
				if (!(matcher = FILE_URL.matcher(get)).find()) {
					LOGGER.info("File with results not found!!!");
				} else {
					sleep(20000, 30000); // wait 20 - 30 seconds
					matcher = SYSNO.matcher(IOUtils.toString(harvest(matcher.group(1))));
					while (matcher.find()) {
						if (matcher.group(1) != null) {
							downloadedKeys.add(SKC_ID_PREFIX + matcher.group(1));
						}
					}
				}
			}
			counterDate = DateUtils.addDays(counterDate, 1); // next day
			if (counterDate.before(toDate)) sleep(120000, 180000); // wait 2-3 minutes
			Set<SkatKey> results = new HashSet<>();
			//get skat keys
			downloadedKeys.forEach(key -> {
				HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(key, Constants.IMPORT_CONF_ID_CASLIN);
				if (hr != null) {
					List<SkatKey> skatkeyList = skatKeyDao.findSkatKeysBySkatId(hr.getId());
					if (skatkeyList != null) results.addAll(skatkeyList);
				}
			});
			results.forEach(key -> key.setManuallyMerged(true));
			pushToDatabase(); // update caslin record
			return results; // update skatKeys, local records from skatKeys
		}
		return null;
	}

	private static void sleep(int min, int max) {
		try {
			int time = min + (int) (Math.random() * ((max - min) + 1));
			LOGGER.info("Waiting: " + time / 1000 + " seconds");
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static InputStream harvest(String url) throws IOException {
		LOGGER.info("Harvesting from: " + url);
		ApacheHttpClient httpClient = new ApacheHttpClient();
		return httpClient.executeGet(url, HEADERS);
	}

	private void pushToDatabase() {
		for (String currentKey : downloadedKeys) {
			push(currentKey);
		}
	}

	private void push(String recordId) {
		String query = "UPDATE harvested_record SET next_dedup_flag=TRUE WHERE import_conf_id = ? AND record_id = ?";
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(query)
				.setLong(0, Constants.IMPORT_CONF_ID_CASLIN)
				.setString(1, recordId).executeUpdate();
	}

	private static String prepareAlephBaseUrl(String date) {
		Map<String, String> params = new HashMap<>();
		params.put("func", "find-c");
		params.put("ccl_term", "ia=sl" + date + '*');
		params.put("adjacent", "N");
		params.put("local_base", "SKC");

		return UrlUtils.buildUrl(ALEPH_URL_QUERY, params);
	}

	private static Map<String, String> createAlephMailParams() {
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
		return params;
	}

	private static String prepareAlephMailUrl(String context) {
		return UrlUtils.buildUrl(ALEPH_URL_QUERY + context, ALEPH_MAIL_PARAMS);
	}

}
