package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AvailabilityFacetEnricher implements DedupRecordEnricher {

	private static final List<String> ONLINE_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	private static final List<String> ONLINE_UNKNOWN_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_UNKNOWN);
	private static final List<String> PROTECTED_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_PROTECTED);

	/**
	 * if any local document contains status online/online or online/unknown
	 * copy this status to others
	 *
	 * @param record         {@link DedupRecord}
	 * @param mergedDocument {@link SolrInputDocument}
	 * @param localRecords   List of {@link SolrInputDocument}
	 */
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
					   List<SolrInputDocument> localRecords) {
		Set<String> enrichingStatuses = new HashSet<>();
		if (localRecords.stream().anyMatch(AvailabilityFacetEnricher::isOnline))
			enrichingStatuses.addAll(ONLINE_STATUSES);
		if (localRecords.stream().anyMatch(AvailabilityFacetEnricher::isOnlineUnknown))
			enrichingStatuses.addAll(ONLINE_UNKNOWN_STATUSES);
		if (localRecords.stream().anyMatch(AvailabilityFacetEnricher::isProtected))
			enrichingStatuses.addAll(PROTECTED_STATUSES);
		if (!enrichingStatuses.isEmpty()) {
			for (SolrInputDocument localRecord : localRecords) {
				Set<Object> statuses = new HashSet<>();
				if (localRecord.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET) != null) {
					statuses.addAll(localRecord.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET));
				}
				statuses.addAll(enrichingStatuses);
				localRecord.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, statuses);
			}
		}
		// remove status facets from merged document
		mergedDocument.remove(SolrFieldConstants.LOCAL_STATUSES_FACET);
	}

	/**
	 * @param doc {@link SolrInputDocument}
	 * @return true if contains "1/online/online" in solr field for statuses or URL with online status
	 */
	private static boolean isOnline(final SolrInputDocument doc) {
		// contains 1/online/online/ ?
		Collection<Object> statuses = doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET);
		if (statuses == null) return false;
		for (Object status : statuses) {
			if (ONLINE_STATUSES.get(1).equals(status)) return true;
		}
		// contains public URL?
		Collection<Object> urls = doc.getFieldValues(SolrFieldConstants.URL);
		if (urls == null) {
			return false;
		}
		for (Object url : urls) {
			if (((String) url).contains("|public|")) return true;
		}
		return false;
	}

	/**
	 * @param doc {@link SolrInputDocument}
	 * @return true if contains "1/online/unknown" in solr field for statuses
	 */
	private static boolean isOnlineUnknown(final SolrInputDocument doc) {
		// contains 1/online/unknown ?
		Collection<Object> statuses = doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET);
		if (statuses == null) return false;
		for (Object status : statuses) {
			if (ONLINE_UNKNOWN_STATUSES.get(1).equals(status)) return true;
		}
		return false;
	}

	/**
	 * @param doc {@link SolrInputDocument}
	 * @return true if contains "|protected|" in solr field for url
	 */
	private static boolean isProtected(final SolrInputDocument doc) {
		// contains protected URL?
		Collection<Object> urls = doc.getFieldValues(SolrFieldConstants.URL);
		if (urls == null) return false;
		for (Object url : urls) {
			if (((String) url).contains("|protected|")) return true;
		}
		return false;
	}

}
