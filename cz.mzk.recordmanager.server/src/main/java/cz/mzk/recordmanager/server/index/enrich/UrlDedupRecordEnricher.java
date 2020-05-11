package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class UrlDedupRecordEnricher implements DedupRecordEnricher {

	@Autowired
	private KramAvailabilityDAO kramAvailabilityDAO;

	private static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");

	private static final List<String> URL_FILTER_LIST = new BufferedReader(new InputStreamReader(
			new ClasspathResourceProvider().getResource("/stopwords/url.txt"), StandardCharsets.UTF_8))
			.lines().collect(Collectors.toCollection(ArrayList::new));
	private static final List<Pattern> URL_PATTERNS = URL_FILTER_LIST.stream()
			.map(url -> Pattern.compile(url, Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {

		Set<Object> urls = new HashSet<>();
		localRecords.stream()
				.filter(rec -> rec.getFieldValue(SolrFieldConstants.URL) != null)
				.forEach(rec -> urls.addAll(rec.getFieldValues(SolrFieldConstants.URL)));

		mergedDocument.remove(SolrFieldConstants.URL);
		mergedDocument.addField(SolrFieldConstants.URL, urlsFilter(urls));

		localRecords.forEach(doc -> doc.remove(SolrFieldConstants.URL));
	}

	/**
	 * @param values urls format "institution code"|"policy code"|"url"
	 * @return List of unique urls
	 */
	private List<String> urlsFilter(Set<Object> values) {
		List<String> results = new ArrayList<>();
		values = filter(values);
		Map<String, TreeSet<EVersionUrl>> urls = new HashMap<>();
		for (Object obj : values) {
			EVersionUrl url;
			try {
				url = EVersionUrl.create(obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			addToMap(urls, url);
		}
		generateUrlFromKramAvailability(urls);
		for (String key : urls.keySet()) {
			boolean online = false;
			boolean protect = false;
			for (EVersionUrl url : urls.get(key).descendingSet()) {
				if (url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_ONLINE)) {
					results.add(url.toString());
					online = true;
				}
				if (!online && url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) {
					results.add(url.toString());
					protect = true;
				}
			}
			if (!online && !protect) {
				if (urls.get(key).size() == 1) {
					results.add(urls.get(key).first().toString());
				} else {
					EVersionUrl url = urls.get(key).first();
					url.setSource("unknown");
					results.add(url.toString());
				}
			}
		}
		return results;
	}

	private void addToMap(Map<String, TreeSet<EVersionUrl>> urls, EVersionUrl url) {
		Matcher matcher;
		String mapKey;

		if ((matcher = UUID_PATTERN.matcher(url.getLink())).find()) {
			mapKey = matcher.group(0);
		} else mapKey = url.getLink();

		if (urls.containsKey(mapKey)) {
			urls.computeIfPresent(mapKey, (key, value) -> value).add(url);
		} else {
			urls.computeIfAbsent(mapKey, key -> new TreeSet<>()).add(url);
		}
	}

	private void generateUrlFromKramAvailability(Map<String, TreeSet<EVersionUrl>> urls) {
		for (String key : urls.keySet()) {
			if (!key.startsWith("uuid:")) continue;
			for (KramAvailability kramAvailability : kramAvailabilityDAO.getByUuid(key)) {
				addToMap(urls, EVersionUrl.create(kramAvailability));
				if (kramAvailability.getHarvestedFrom().getIdPrefix().equals(Constants.PREFIX_KRAM_MZK)
						&& kramAvailability.getAvailability().equals("private")) {
					addToMap(urls, EVersionUrl.create(Constants.PREFIX_KRAM_MZK_VS,
							kramAvailability.getAvailability(),
							"https://kramerius-vs.mzk.cz/view/" + key,
							Constants.KRAM_VS_COMMENT));
				}
			}
		}
	}

	/**
	 * remove unneeded url
	 *
	 * @param urls original
	 * @return filtered urls
	 */
	private Set<Object> filter(Set<Object> urls) {
		return urls.stream().filter(
				url -> URL_PATTERNS.stream().noneMatch(pat -> pat.matcher(url.toString()).find()))
				.collect(Collectors.toSet());
	}

}
