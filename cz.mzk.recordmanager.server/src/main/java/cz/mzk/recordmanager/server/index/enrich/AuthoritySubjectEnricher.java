package cz.mzk.recordmanager.server.index.enrich;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class AuthoritySubjectEnricher extends AuthorityEnricher implements DedupRecordEnricher{
	
	private static HashMap<String, String> localAuthFieldMap = new HashMap<>();
	{
		localAuthFieldMap.put("600", "400");
		localAuthFieldMap.put("610", "410");
		localAuthFieldMap.put("611", "411");
		localAuthFieldMap.put("648", "448");
		localAuthFieldMap.put("650", "450");
		localAuthFieldMap.put("651", "451");
	}
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<String> result = new HashSet<>();
		
		result.addAll(getSolrField(localRecords, SolrFieldConstants.SUBJECT_AUTHORITY_DUMMY_FIELD, localAuthFieldMap));
		
		mergedDocument.addField(SolrFieldConstants.SUBJECT_VIZ_FIELD, result);
	}
}
