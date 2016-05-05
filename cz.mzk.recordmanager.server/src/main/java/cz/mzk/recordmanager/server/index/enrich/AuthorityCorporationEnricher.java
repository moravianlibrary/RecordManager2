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
public class AuthorityCorporationEnricher extends AuthorityEnricher implements DedupRecordEnricher{

	private static HashMap<String, String> localAuthFieldMap = new HashMap<>();
	{
		localAuthFieldMap.put("110", "410");
		localAuthFieldMap.put("111", "411");
		localAuthFieldMap.put("710", "410");
		localAuthFieldMap.put("711", "411");
	}
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<String> result = new HashSet<>();
		
		result.addAll(getSolrField(localRecords, SolrFieldConstants.CORPORATION_AUTHORITY_DUMMY_FIELD, localAuthFieldMap));
		
		mergedDocument.addField(SolrFieldConstants.CORPORATION_VIZ_FIELD, result);
	}
}
