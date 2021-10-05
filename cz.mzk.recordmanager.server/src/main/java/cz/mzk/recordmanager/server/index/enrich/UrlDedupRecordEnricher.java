package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;
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
		mergedDocument.addField(SolrFieldConstants.URL, urlsFilter(mergedDocument, localRecords, urls));

		localRecords.forEach(doc -> doc.remove(SolrFieldConstants.URL));

		enrichStatusesFacet(mergedDocument, localRecords);
	}

	/**
	 * @param values urls format "institution code"|"policy code"|"url"
	 * @return List of unique urls
	 */
	private List<String> urlsFilter(SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords,
			Set<Object> values) {
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
		boolean potentialDnnt = false;
		if (mergedDocument.containsKey(SolrFieldConstants.POTENTIAL_DNNT)
				&& mergedDocument.getFieldValue(SolrFieldConstants.POTENTIAL_DNNT) != null) {
			potentialDnnt = (boolean) mergedDocument.getFieldValue(SolrFieldConstants.POTENTIAL_DNNT);
		}
		generateUrlFromKramAvailability(potentialDnnt, urls);
		createArticleUrl(localRecords, potentialDnnt, urls);
		for (String key : urls.keySet()) {
			boolean online = false;
			boolean protect = false;
			boolean dnnt = false;
			for (EVersionUrl url : urls.get(key).descendingSet()) {
				if (url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_ONLINE)) {
					results.add(url.toString());
					online = true;
				}
				if (!online && url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_DNNT)) {
					results.add(url.toString());
					dnnt = true;
				}
				if (!online && !dnnt && url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) {
					results.add(url.toString());
					protect = true;
				}
			}
			if (!online && !dnnt && !protect) {
				if (urls.get(key).size() == 1) {
					results.add(urls.get(key).first().toString());
				} else {
					EVersionUrl url = urls.get(key).first();
					url.setSource(Constants.DOCUMENT_AVAILABILITY_UNKNOWN);
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

	private void generateUrlFromKramAvailability(boolean potentialDnnt, Map<String, TreeSet<EVersionUrl>> urls) {
		for (String key : urls.keySet()) {
			if (!key.startsWith("uuid:")) continue;
			for (KramAvailability kramAvailability : kramAvailabilityDAO.getByUuid(key)) {
				EVersionUrl newUrl = EVersionUrl.create(kramAvailability, potentialDnnt);
				addToMap(urls, newUrl);
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

	private void enrichStatusesFacet(SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		Collection<Object> urls = mergedDocument.getFieldValues(SolrFieldConstants.URL);
		if (urls == null) return;
		Set<String> availabilitiesSimple = new HashSet<>();
		for (Object url : urls) {
			try {
				availabilitiesSimple.add(EVersionUrl.create(url.toString()).getAvailability());
			} catch (Exception ignore) {
			}
		}
		if (availabilitiesSimple.isEmpty()) return;
		Set<String> availabilities = new HashSet<>(); // hierarchical
		for (String availability : availabilitiesSimple) {
			if (availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) continue;
			availabilities.addAll(SolrUtils.createHierarchicFacetValues(
					Constants.DOCUMENT_AVAILABILITY_ONLINE, availability));
		}
		// hierarchical to all local records
		for (SolrInputDocument localRecord : localRecords) {
			Set<String> results = localRecord.containsKey(SolrFieldConstants.LOCAL_STATUSES_FACET)
					? localRecord.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).stream()
					.map(Object::toString).collect(Collectors.toSet())
					: new HashSet<>();
			results.addAll(availabilities);
			localRecord.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, results);
		}
		// simple to merged record
		mergedDocument.setField(SolrFieldConstants.STATUSES_FACET, availabilitiesSimple);
	}

	private void createArticleUrl(List<SolrInputDocument> localRecords, boolean potentialDnnt, final Map<String, TreeSet<EVersionUrl>> urls) {
		Set<String> articleKeys = new HashSet<>();
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants.ARTICLE_AVAILABILITY_KEY)
					&& localRecord.getFieldValue(SolrFieldConstants.ARTICLE_AVAILABILITY_KEY) != null) {
				articleKeys.add(localRecord.getFieldValue(SolrFieldConstants.ARTICLE_AVAILABILITY_KEY).toString());
			}
		}
		for (String articleKey : articleKeys) {
			Map<Long, List<KramAvailability>> map = new HashMap<>();
			List<KramAvailability> list = kramAvailabilityDAO.getByArticleKey(articleKey);
			// page with same key must have same parent (issue)
			if (list.stream().map(a -> a.getParentUuid()).collect(Collectors.toSet()).size() != 1) continue;
			for (KramAvailability availability : list) {
				Long importConfId = availability.getHarvestedFrom().getId();
				if (map.containsKey(importConfId)) {
					map.computeIfPresent(importConfId, (key, value) -> value).add(availability);
				} else {
					map.computeIfAbsent(importConfId, key -> new ArrayList<>()).add(availability);
				}
			}
			for (Long importConfId : map.keySet()) {
				List<KramAvailability> availabilities = map.get(importConfId);
				if (availabilities.size() == 1)
					addToMap(urls, EVersionUrl.create(availabilities.get(0), potentialDnnt));
				else if (availabilities.size() > 1) {
					// same key, same parent_uuid, link to parent -> issue
					KramAvailability availability = availabilities.get(0);
					KramAvailability parent = kramAvailabilityDAO.getByConfigAndUuid(availability.getHarvestedFrom(), availability.getParentUuid());
					if (parent != null) addToMap(urls, EVersionUrl.create(parent, potentialDnnt));
				}
			}
		}
	}

}
