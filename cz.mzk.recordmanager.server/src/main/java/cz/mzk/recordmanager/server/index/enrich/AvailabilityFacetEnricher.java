package cz.mzk.recordmanager.server.index.enrich;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class AvailabilityFacetEnricher implements DedupRecordEnricher {
	
	private final List<String> ONLINE_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	private final List<String> ONLINE_UNKNOWN_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_UNKNOWN);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		boolean online = localRecords.stream().anyMatch(rec -> isOnline(rec));
		if (online) {
			localRecords.forEach(doc -> {
				Set<Object> statuses = new HashSet<>();
				if (doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET) != null) {
					statuses.addAll(doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET));
				}
				statuses.addAll(ONLINE_STATUSES);
				doc.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, statuses);
				
			});
		}
		boolean unknown = localRecords.stream().anyMatch(rec -> isOnlineUnknown(rec));
		if (unknown) {
			localRecords.forEach(doc -> {
				Set<Object> statuses = new HashSet<>();
				if (doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET) != null) {
					statuses.addAll(doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET));
				}
				statuses.addAll(ONLINE_UNKNOWN_STATUSES);
				doc.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, statuses);
			});
		}

		// remove status facets from merged document
		mergedDocument.remove(SolrFieldConstants.LOCAL_STATUSES_FACET);
	}

	protected boolean isOnline(SolrInputDocument doc) {
		// contains 1/online/online/ ?
		Collection<Object> statuses = doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET);
		if (statuses == null) return false;
		for (Object status: statuses) {
			if (ONLINE_STATUSES.get(1).equals(status)) return true;
		}
		
		// contains online URL?
		Collection<Object> urls = doc.getFieldValues(SolrFieldConstants.URL);
		if (urls == null) {
			return false;
		}
		for (Object url : urls) {
			String[] splited = ((String) url).split("\\|");
			if (Constants.DOCUMENT_AVAILABILITY_ONLINE.equalsIgnoreCase(splited[1])) {
				return true;
			}
		}
		return false;
	}

	protected boolean isOnlineUnknown(SolrInputDocument doc){
		// contains 1/online/unknown ?
		Collection<Object> statuses = doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET);
		if (statuses == null) return false;
		for (Object status: statuses) {
			if (ONLINE_UNKNOWN_STATUSES.get(1).equals(status)) return true;
		}
		
		return false;
	}
	
}
