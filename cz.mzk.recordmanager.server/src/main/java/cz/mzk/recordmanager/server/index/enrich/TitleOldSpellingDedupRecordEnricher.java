package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.TitleOldSpelling;
import cz.mzk.recordmanager.server.oai.dao.hibernate.TitleOldSpellingDAOHibernate;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TitleOldSpellingDedupRecordEnricher implements DedupRecordEnricher {

	@Autowired
	private TitleOldSpellingDAOHibernate titleOldSpellingDAOHibernate;

	private static final int LRU_CACHE_SIZE = 20000;

	private final Map<String, String> cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE));

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		if (!mergedDocument.containsKey(SolrFieldConstants.PUBLISHDATE_TXT_MV)
				|| mergedDocument.getFieldValues(SolrFieldConstants.PUBLISHDATE_TXT_MV) == null
				|| mergedDocument.getFieldValues(SolrFieldConstants.PUBLISHDATE_TXT_MV).stream().noneMatch(n -> (Integer) n <= 1957)
				|| !mergedDocument.containsKey(SolrFieldConstants.LANGUAGE_TXT_MV)
				|| mergedDocument.getFieldValues(SolrFieldConstants.LANGUAGE_TXT_MV).stream().noneMatch(s -> s.toString().equalsIgnoreCase("czech")))
			return;
		List<String> results = new ArrayList<>();
		Set<String> titles = new HashSet<>();
		if (mergedDocument.containsKey(SolrFieldConstants._HIDDEN_TITLE_SEARCH_TXT_MV)) {
			titles = mergedDocument.getFieldValues(SolrFieldConstants._HIDDEN_TITLE_SEARCH_TXT_MV).stream().map(s -> (String) s).collect(Collectors.toSet());
		} else {
			for (SolrInputDocument localRecord : localRecords) {
				if (localRecord.containsKey(SolrFieldConstants._HIDDEN_TITLE_SEARCH_TXT_MV)) {
					titles.addAll(localRecord.getFieldValues(SolrFieldConstants._HIDDEN_TITLE_SEARCH_TXT_MV).stream()
							.map(s -> ((String) s).toLowerCase()).collect(Collectors.toSet()));
				}
			}
		}
		if (titles.isEmpty()) return;
		for (String title : titles) {
			List<String> partResults = new ArrayList<>();
			for (String word : title.split("\\b")) {
				String alterWord = null;
				if (cache.containsKey(word)) {
					alterWord = cache.get(word);
				} else {
					TitleOldSpelling titleOldSpelling = titleOldSpellingDAOHibernate.findByKey(word);
					if (titleOldSpelling != null) {
						alterWord = titleOldSpelling.getValue();
						cache.put(word, alterWord);
					}
				}
				List<String> temp = new ArrayList<>();
				for (String alterTitle : partResults) {
					temp.add(alterTitle + word);
				}
				if (partResults.isEmpty()) temp.add(word);
				if (alterWord != null) {
					for (String alterTitle : partResults) {
						temp.add(alterTitle + alterWord);
					}
					if (partResults.isEmpty()) temp.add(alterWord);
				}
				partResults = temp;
			}
			results.addAll(partResults);
		}
		results.removeAll(titles);
		mergedDocument.setField(SolrFieldConstants.TITLE_OLD_SPELLING, results);
	}

}
