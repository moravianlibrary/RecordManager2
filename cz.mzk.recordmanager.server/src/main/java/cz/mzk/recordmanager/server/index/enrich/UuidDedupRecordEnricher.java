package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class UuidDedupRecordEnricher implements DedupRecordEnricher {
	private static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		if (!mergedDocument.containsKey(SolrFieldConstants.URL)
				|| mergedDocument.getFieldValues(SolrFieldConstants.URL) == null
				|| mergedDocument.getFieldValues(SolrFieldConstants.URL).isEmpty()) {
			return;
		}
		List<String> urls = mergedDocument.getFieldValues(SolrFieldConstants.URL).stream()
				.map(o -> o.toString()).collect(Collectors.toList());
		Set<String> uuids = new HashSet<>();

		for (String url : urls) {
			Matcher matcher = UUID_PATTERN.matcher(url);
			if (matcher.find()) {
				uuids.add(matcher.group(0));
			}
		}
		if (!uuids.isEmpty()) {
			mergedDocument.setField(SolrFieldConstants.UUIDS, uuids);
		}
	}

}
