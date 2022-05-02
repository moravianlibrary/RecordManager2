package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SitemapDedupRecordEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument, List<SolrInputDocument> localRecords) {
		SolrInputDocument leading = localRecords.get(0);
		List<Integer> years = getValues(mergedDocument, SolrFieldConstants.PUBLISH_DATE_FACET)
				.stream().map(o -> (Integer) o).collect(Collectors.toList());
		List<String> langs = getValues(mergedDocument, SolrFieldConstants.LANGUAGE_FACET)
				.stream().map(o -> (String) o).collect(Collectors.toList());
		List<String> statuses = getValues(mergedDocument, SolrFieldConstants.STATUSES_FACET)
				.stream().map(o -> (String) o).collect(Collectors.toList());
		List<String> formats = getValues(mergedDocument, SolrFieldConstants.RECORD_FORMAT_FACET)
				.stream().map(o -> (String) o).collect(Collectors.toList());
		if (isSitemap(years, Calendar.getInstance().get(Calendar.YEAR) - 5,
				formats, langs, localRecords.size(), 25)
				|| isSitemap(years, Calendar.getInstance().get(Calendar.YEAR) - 15,
				formats, langs, localRecords.size(), 35)
				|| isSitemap(statuses, localRecords.size(), 25)) {
			leading.addField(SolrFieldConstants.SITEMAP, Collections.singletonList("cpk"));
		}
	}

	private List<Object> getValues(SolrInputDocument mergedDocument, String field) {
		if (!mergedDocument.containsKey(field) || mergedDocument.getFieldValues(field) == null)
			return Collections.emptyList();
		return new ArrayList<>(mergedDocument.getFieldValues(field));
	}

	private boolean isSitemap(List<Integer> years, int limitYear, List<String> formats, List<String> langs,
			int mergedRecords, int limitMergedRecords) {
		return years.stream().anyMatch(y -> y > limitYear)
				&& formats.contains("0/BOOKS/")
				&& langs.contains("Czech")
				&& mergedRecords >= limitMergedRecords;
	}

	private static final List<String> statuses = Arrays.asList("dnnt", "online");

	private boolean isSitemap(List<String> statusesOrig, int mergedRecords, int limitMergedRecords) {
		return !Collections.disjoint(statusesOrig, statuses) && mergedRecords >= limitMergedRecords;
	}

}
