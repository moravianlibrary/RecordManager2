package cz.mzk.recordmanager.server.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.solr.client.solrj.util.ClientUtils;

public class SolrUtils {

	private static final String WILDCARD = "*";

	private static final String RANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static String createFieldQuery(String field, String value) {
		return String.format("%s:%s", field, value);
	}

	public static String createRange(String from, String to) {
		from = (from != null)? ClientUtils.escapeQueryChars(from) : WILDCARD;
		to = (to != null)? ClientUtils.escapeQueryChars(to) : WILDCARD;
		return String.format("[%s TO %s]", from,
				to);
	}

	public static String createDateRange(Date from, Date until) {
		// 'Z' on the end is necessary.. could not find a way how to put one
		// letter timezone and other letters than Z do not seem to work with
		// SOLR via Kramerius API anyway
		DateFormat df = new SimpleDateFormat(RANGE_DATE_FORMAT);
		String fromStr = (from != null) ? df.format(from) : null;
		String untilStr = (until != null) ? df.format(until) : null;
		return createRange(fromStr, untilStr);
	}

}
