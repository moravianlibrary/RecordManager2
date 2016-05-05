package cz.mzk.recordmanager.server.index.enrich;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class AuthorityAuthorEnricher extends AuthorityEnricher implements DedupRecordEnricher{

	private static HashMap<String, String> localAuthFieldMap = new HashMap<>();
	{
		localAuthFieldMap.put("100", "400");
		localAuthFieldMap.put("700", "400");
	}
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<String> result = null;
		
		result = getSolrField(localRecords, SolrFieldConstants.AUTHOR_AUTHORITY_DUMMY_FIELD, localAuthFieldMap);

		mergedDocument.addField(SolrFieldConstants.AUTHOR_VIZ_FIELD, result);
	}
}
