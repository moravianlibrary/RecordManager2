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

	private final Pattern GLOBAL_AVAILABILITY_INSTITUTION_PATTERN = Pattern.compile("^sfx.*");

	private final List<String> ONLINE_STATUSES = SolrUtils.createHierarchicFacetValues("online", Constants.DOCUMENT_AVAILABILITY_ONLINE);

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		boolean online = localRecords.stream().anyMatch(rec -> isOnline(rec));
		if (online) {
			localRecords.forEach(doc -> doc.setField(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_STATUSES));
		}
		// remove status facets from merged document
		mergedDocument.remove(SolrFieldConstants.LOCAL_STATUSES_FACET);
	}

	protected boolean isOnline(SolrInputDocument doc) {
		// Is from SFX?
		String id = (String) doc.getFieldValue(SolrFieldConstants.ID_FIELD);
		if (GLOBAL_AVAILABILITY_INSTITUTION_PATTERN.matcher(id).matches()) {
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

}
