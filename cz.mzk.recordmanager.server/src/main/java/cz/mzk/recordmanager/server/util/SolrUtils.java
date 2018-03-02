package cz.mzk.recordmanager.server.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.ImportConfiguration;

public class SolrUtils {

	private static final String WILDCARD = "*";

	private static final String RANGE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static final char HIERARCHIC_FACET_SEPARATOR = '/';

	private static final Pattern RECORDTYPE_PATTERN = Pattern.compile("^(AUDIO|VIDEO|OTHER|LEGISLATIVE|PATENTS)_(.*)$");
	
	private static final String INSTITUTION_LIBRARY = "Library";
	private static final String INSTITUTION_OTHERS = "Others";
	private static final String INSTITUTION_UNKNOWN = "unknown";

	private enum WEIGHT_DOC_COMPARATOR implements Comparator<SolrInputDocument> {
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
		Preconditions.checkNotNull(document, "document");
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

	public static List<String> createRecordTypeHierarchicFacet(HarvestedRecordFormatEnum format) {
		Matcher matcher = RECORDTYPE_PATTERN.matcher(format.name());
		if (matcher.matches()) {
			return SolrUtils.createHierarchicFacetValues(matcher.group(1), matcher.group(2));
		}
		else {
			return SolrUtils.createHierarchicFacetValues(format.name());
		}
	}

	public static List<String> createRecordTypeHierarchicFacet(Collection<HarvestedRecordFormatEnum> formats) {
		List<String> results = new ArrayList<>();
		for (HarvestedRecordFormatEnum format : formats) {
			results.addAll(createRecordTypeHierarchicFacet(format));
		}
		return results;
	}

	protected static String getInstitutionOfRecord(ImportConfiguration config) {
		if (config != null && config.getLibrary() != null
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	protected static String getCityOfRecord(ImportConfiguration config) {
		if (config != null && config.getLibrary() != null
				&& config.getLibrary().getCity() != null) {
			return config.getLibrary().getCity();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

	protected static String getPrefixOfNKPRecord(ImportConfiguration config) {
		if (config != null) {
			if (config.getId() != null) {
				if (config.getId() == Constants.IMPORT_CONF_ID_SLK)
					return Constants.LIBRARY_NAME_SLK;
				if (config.getId() == Constants.IMPORT_CONF_ID_KKL)
					return Constants.LIBRARY_NAME_KKL;
				if (config.getId() == Constants.IMPORT_CONF_ID_STT)
					return Constants.LIBRARY_NAME_STT;
			}
			if (config.getIdPrefix() != null) {
				return config.getIdPrefix().toUpperCase();
			}
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}
	
	public static List<String> getInstitution(ImportConfiguration config) {
		if (config != null) {
			String city = getCityOfRecord(config);
			city = city == null ? INSTITUTION_UNKNOWN : MetadataUtils.normalize(city);
			String name = getInstitutionOfRecord(config);
			if (name.equals(Constants.LIBRARY_NAME_NKP)) {
				String prefix = getPrefixOfNKPRecord(config);
				return SolrUtils.createHierarchicFacetValues(INSTITUTION_LIBRARY, city, name, prefix);
			}
			String base = config.isLibrary() ? INSTITUTION_LIBRARY : INSTITUTION_OTHERS;
			if (city.equals(INSTITUTION_UNKNOWN)) return SolrUtils.createHierarchicFacetValues(base, name);
			else return SolrUtils.createHierarchicFacetValues(base, city, name);
		}

		return SolrUtils.createHierarchicFacetValues(INSTITUTION_UNKNOWN);
	}

}
