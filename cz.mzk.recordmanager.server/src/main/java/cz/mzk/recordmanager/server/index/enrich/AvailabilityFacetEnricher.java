package cz.mzk.recordmanager.server.index.enrich;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class AvailabilityFacetEnricher implements DedupRecordEnricher {

	
	private final Pattern GLOBAL_AVAILABILITY_INSTITUTION_PATTERN = Pattern.compile("^sfx.*");
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		
		boolean addOnlineToAll = false;
		// add 'online' to all records if:
		//  a) at least one local record is from sfx*
		//  b) at least one local record has url with online availability 
		for (SolrInputDocument doc: localRecords) {
			// a)
			Object id = doc.getFieldValue(SolrFieldConstants.ID_FIELD);
			if (id instanceof String) {
				if (GLOBAL_AVAILABILITY_INSTITUTION_PATTERN.matcher((String) id).matches()) {
					addOnlineToAll = true;
					break;
				}
			}
			
			// b)
			Collection<Object> urls = doc.getFieldValues(SolrFieldConstants.URL);
			if (urls == null) {
				continue;
			}
			for (Object url: urls) {
				if (url instanceof String) {
					String[] splited = ((String) url).split("\\|");
					if (splited.length == 3 && Constants.DOCUMENT_AVAILABILITY_ONLINE.equalsIgnoreCase(splited[1])) {
						addOnlineToAll = true;
						break;
					}
					
				}
			}
		}
		
		if (addOnlineToAll) {
			boolean hasOnlineStatus = false;
			String onlineFacetStatusParent = "0/online/";
			String onlineFacetStatus = "1/online/" + Constants.DOCUMENT_AVAILABILITY_ONLINE + "/";
			for (SolrInputDocument doc: localRecords) {
				Collection<Object> statuses = doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET);
				for (Object status: statuses) {
					if (status instanceof String) {
						hasOnlineStatus |= ((String) status).equalsIgnoreCase(onlineFacetStatus);
					}
				}
				if (!hasOnlineStatus) {
					statuses.add(onlineFacetStatusParent);
					statuses.add(onlineFacetStatus);
					doc.removeField(SolrFieldConstants.LOCAL_STATUSES_FACET);
					doc.addField(SolrFieldConstants.LOCAL_STATUSES_FACET, statuses);
				}
				
			}
		}
		
		// remove status facets from merged document
		mergedDocument.remove(SolrFieldConstants.LOCAL_STATUSES_FACET);
		
	}


}
