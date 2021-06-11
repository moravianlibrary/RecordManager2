package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TitleSearchDedupRecordEnricher implements DedupRecordEnricher {

	private static final Pattern TITLE_TO_BLANK = Pattern.compile("['\\[\\]\"`!()\\-{};:.,?/@*%=^_|~\\s]*$");

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		Set<String> allTitles = new HashSet<>();
		if (mergedDocument.containsKey(SolrFieldConstants.TITLE) && mergedDocument.getFieldValue(SolrFieldConstants.TITLE) != null) {
			allTitles.add(mergedDocument.getFieldValue(SolrFieldConstants.TITLE).toString().toLowerCase());
		}
		if (mergedDocument.containsKey(SolrFieldConstants.TITLE_SERIES_SEARCH)) {
			allTitles.addAll(mergedDocument.getFieldValues(SolrFieldConstants.TITLE_SERIES_SEARCH).stream()
					.map(t -> t.toString().toLowerCase()).collect(Collectors.toSet()));
		}
		allTitles = CleaningUtils.replaceFirst(allTitles, TITLE_TO_BLANK, "");
		Set<String> localTitles = new HashSet<>();
		for (SolrInputDocument localRecord : localRecords) {
			if (localRecord.containsKey(SolrFieldConstants._HIDDEN_TITLE_SEARCH_TXT_MV)) {
				Set<String> finalAllTitles = allTitles;
				localTitles.addAll(localRecord.getFieldValues(SolrFieldConstants._HIDDEN_TITLE_SEARCH_TXT_MV)
						.stream().map(t -> CleaningUtils.replaceFirst(t.toString().toLowerCase(), TITLE_TO_BLANK, "")).collect(Collectors.toSet())
						.stream().filter(t -> !finalAllTitles.contains(t))
						.collect(Collectors.toSet()));
			}
		}
		if (!localTitles.isEmpty()) {
			mergedDocument.addField(SolrFieldConstants.TITLE_SEARCH_TXT_MV, localTitles);
		}
	}

}
