package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocalMzkStatusesDedupRecordEnricher implements DedupRecordEnricher {


	private static final List<String> ALLOWED_STATUSES = Arrays.asList(
			Constants.DOCUMENT_AVAILABILITY_ONLINE,
			Constants.DOCUMENT_AVAILABILITY_DNNT
	);


	private static final List<String> SOURCES = Arrays.asList(
			Constants.PREFIX_MZK,
			Constants.PREFIX_KNAV,
			Constants.PREFIX_NKP,
			Constants.PREFIX_SLK,
			Constants.PREFIX_STT,
			Constants.PREFIX_KKL,
			Constants.PREFIX_ANL
	);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		// exists statuses?
		if (!mergedDocument.containsKey(SolrFieldConstants.STATUSES_FACET) ||
				mergedDocument.getFieldValues(SolrFieldConstants.STATUSES_FACET) == null) {
			return;
		}

		// all statuses
		List<String> statuses = mergedDocument.getFieldValues(SolrFieldConstants.STATUSES_FACET).stream()
				.map(o -> o.toString()).collect(Collectors.toList());

		for (String source : SOURCES) {
			// exists records for source?
			List<SolrInputDocument> sourceRecords = new ArrayList<>();
			for (SolrInputDocument localRecord : localRecords) {
				if (localRecord.getFieldValue(SolrFieldConstants.ID_FIELD).toString().startsWith(source + ".")
						|| localRecord.getFieldValue(SolrFieldConstants.ID_FIELD).toString().startsWith("bookport.")) {
					sourceRecords.add(localRecord);
				}
			}
			if (sourceRecords.isEmpty()) continue;

			// filter
			Set<String> localStatuses = statuses.stream().filter(s -> ALLOWED_STATUSES.contains(s)).collect(Collectors.toSet());

			List<EVersionUrl> urls = new ArrayList<>();
			if (mergedDocument.containsKey(SolrFieldConstants.URL) &&
					mergedDocument.getFieldValues(SolrFieldConstants.URL) != null) {
				urls = mergedDocument.getFieldValues(SolrFieldConstants.URL).stream().map(o -> {
					try {
						return EVersionUrl.create(o.toString());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList());
			}
			// filter protected links
			for (EVersionUrl url : urls) {
				if (url.getSource().equals("kram-" + source) && url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) {
					localStatuses.add(Constants.DOCUMENT_AVAILABILITY_PROTECTED);
				}
			}

			// filter bookport
			if (mergedDocument.getFieldValues(SolrFieldConstants.LOCAL_IDS_FIELD).stream().anyMatch(i -> i.toString().startsWith("bookport."))) {
				for (EVersionUrl url : urls) {
					if (url.getSource().equals(source) && url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_MEMBER)) {
						localStatuses.add(Constants.DOCUMENT_AVAILABILITY_BOOKPORT);
					}
				}
			}
			if (localStatuses.isEmpty()) return;
			for (SolrInputDocument solrDoc : sourceRecords) {
				solrDoc.setField(SolrFieldConstants.LOCAL_ONLINE_FACET, localStatuses);
			}
		}
	}
}
