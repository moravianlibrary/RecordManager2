package cz.mzk.recordmanager.server.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;

public class SolrUtils {

	private static final String WILDCARD = "*";

	private static final String RANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static final char HIERARCHIC_FACET_SEPARATOR = '/';

	private static enum WEIGHT_DOC_COMPARATOR implements Comparator<SolrInputDocument> {
		INSTANCE;

		@Override
		public int compare(SolrInputDocument first, SolrInputDocument second) {
			int firstWeight = getWeight(first);
			int secondWeight = getWeight(second);
			return secondWeight - firstWeight;
		}

		private int getWeight(SolrInputDocument document) {
			int weight = 0;
			Object value = document.getFieldValue(SolrFieldConstants.WEIGHT);
			if (value != null && value instanceof Integer) {
				weight = (Integer) value;
			}
			return weight;
		}

	}

	public static String escape(String val) {
		return ClientUtils.escapeQueryChars(val);
	}

	public static String createFieldQuery(String field, String value) {
		return String.format("%s:%s", field, value);
	}

	public static String createEscapedFieldQuery(String field, String value) {
		return String.format("%s:%s", field, escape(value));
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

	public static void sortByWeight(List<SolrInputDocument> childs) {
		Collections.sort(childs, WEIGHT_DOC_COMPARATOR.INSTANCE);
	}

	public static List<SolrInputDocument> removeHiddenFields(List<SolrInputDocument> documents) {
		for (SolrInputDocument document : documents) {
			removeHiddenFields(document);
		}
		return documents;
	}

	public static SolrInputDocument removeHiddenFields(SolrInputDocument document) {
		Iterator<String> iter = document.getFieldNames().iterator();
		while (iter.hasNext()) {
			String field = iter.next();
			if (field.startsWith(SolrFieldConstants.SOLR_HIDDEN_FIELD_PREFIX)) {
				iter.remove();
			}
		}
		if (document.hasChildDocuments()) {
			removeHiddenFields(document.getChildDocuments());
		}
		return document;
	}

	/**
	 *
	 * Create hierarchic facet values, ie. for input Brno, MZK returns:
	 *
	 * 0/Brno/
	 * 1/Brno/MZK/
	 *
	 * @param values
	 * @return hierarchic facet values
	 */
	public static List<String> createHierarchicFacetValues(String...values) {
		List<String> result = new ArrayList<>(values.length);
		for (int i = 0; i != values.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i);
			sb.append(HIERARCHIC_FACET_SEPARATOR);
			for (int j = 0; j!= i + 1; j++) {
				sb.append(values[j]);
				sb.append(HIERARCHIC_FACET_SEPARATOR);
			}
			result.add(sb.toString());
		}
		return result;
	}

}
