package cz.mzk.recordmanager.server.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class SolrUtils {

	private static final String RANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static String createQueryString(Map<String, String> query) {
		StringBuilder queryString = new StringBuilder();
		Iterator<String> iterator = query.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = query.get(key);
			queryString.append(key + ":" + value);
			if (iterator.hasNext()) {
				queryString.append(" AND ");
			}
		}
		return queryString.toString();
	}

	public static String createSolrDateRange(Date from, Date until) { 
		// 'Z' on the end is necessary.. could not find a way how to put one
		// letter timezone and other letters than Z do not seem to work with
		// SOLR via Kramerius API anyway
		DateFormat df = new SimpleDateFormat(RANGE_DATE_FORMAT);
		String fromStr = (from != null) ? df.format(from) : "*";
		String untilStr = (until != null) ? df.format(until) : "*";
		return String.format("[%s TO %s]", fromStr,
				untilStr);
	}

}
