package cz.mzk.recordmanager.server.index.enrich;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class AvailabilityFacetEnricher implements DedupRecordEnricher {

	private final Pattern GLOBAL_ONLINE_AVAILABILITY_INSTITUTION_PATTERN = Pattern.compile("^(mkpe|zakony|upv).*");
	private final Pattern GLOBAL_UNKNOWN_AVAILABILITY_INSTITUTION_PATTERN = Pattern.compile("^sfx.*");

	private static final String ONLINE = "online";
	
	private final List<String> ONLINE_STATUSES = SolrUtils.createHierarchicFacetValues(ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	private final List<String> ONLINE_UNKNOWN_STATUSES = SolrUtils.createHierarchicFacetValues(ONLINE, Constants.DOCUMENT_AVAILABILITY_UNKNOWN);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		boolean online = localRecords.stream().anyMatch(rec -> isOnline(rec));
		if (online) {
			localRecords.forEach(doc -> doc.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_STATUSES));
		}
		else {
			boolean unknown = localRecords.stream().anyMatch(rec -> isOnlineUnknown(rec));
			if (unknown) {
				localRecords.forEach(doc -> doc.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_UNKNOWN_STATUSES));
			}
		}
		// remove status facets from merged document
		mergedDocument.remove(SolrFieldConstants.LOCAL_STATUSES_FACET);
	}

	protected boolean isOnline(SolrInputDocument doc) {
		String id = (String) doc.getFieldValue(SolrFieldConstants.ID_FIELD);
		if (GLOBAL_ONLINE_AVAILABILITY_INSTITUTION_PATTERN.matcher(id).matches()) {
			return true;
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
		// Is from SFX?
		String id = (String) doc.getFieldValue(SolrFieldConstants.ID_FIELD);
		if (GLOBAL_UNKNOWN_AVAILABILITY_INSTITUTION_PATTERN.matcher(id).matches()) {
			return true;
		}
		return false;
	}
	
}
