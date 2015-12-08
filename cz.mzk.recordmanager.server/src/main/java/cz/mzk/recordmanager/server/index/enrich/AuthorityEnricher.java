package cz.mzk.recordmanager.server.index.enrich;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.AuthorityRecordDAO;

@Component
public class AuthorityEnricher implements DedupRecordEnricher {

	@Autowired
	private AuthorityRecordDAO authorityRecordDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	private final int LRU_CACHE_SIZE = 50000;
	
	private final Map<String, String> field400Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE));
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<Object> authorityKeysSet = new HashSet<>();
		Set<String> authorsSort = new HashSet<>();

		// extract all existing authors from local records
		localRecords.stream()
			.filter(r -> r.containsKey(SolrFieldConstants.AUTHOR_DUMMY_FIELD)) //
	    	.forEach(r -> r.getFieldValues(SolrFieldConstants.AUTHOR_DUMMY_FIELD) //
	    			.stream().filter(s -> (s instanceof String)).forEach(s -> authorsSort.add((String) s)) //
	    );
		
		// extract all authority keys from local records
		localRecords.stream()
			.filter(r -> r.containsKey(SolrFieldConstants.AUTHOR_AUTHORITY_DUMMY_FIELD)) //
		    .forEach(r -> r.getFieldValues(SolrFieldConstants.AUTHOR_AUTHORITY_DUMMY_FIELD) //
		    	.stream().filter(s -> (s instanceof String)).forEach(s -> authorityKeysSet.add((String) s)) //
		);
		
		for (Object currentAuthKey: authorityKeysSet) {
			if (!(currentAuthKey instanceof String)) {
				continue;
			}
			String currentAuthKeyStr = (String) currentAuthKey;
			// check cache for field value
			if (field400Cache.containsKey(currentAuthKey)) {
				authorsSort.add(field400Cache.get(currentAuthKey));
			} else {
				// parse value from authority record
				AuthorityRecord auth = authorityRecordDao.findByAuthKey(currentAuthKeyStr);
				if (auth == null || auth.getRawRecord() == null) {
					continue;
				}
				InputStream is = new ByteArrayInputStream(auth.getRawRecord());
				MarcRecord marc = null;
				try {
					marc = marcXmlParser.parseRecord(is);
				} catch (InvalidMarcException ime) {
					continue;
				}
				
				String author400Field = marc.getField("400", 'a', 'b', 'c', 'd');
				if (author400Field != null && !author400Field.isEmpty()) {
					authorsSort.add(author400Field);
					field400Cache.put(auth.getAuthorityCode(), author400Field);
				}
				
				
			}
			
		}
		
		mergedDocument.addField(SolrFieldConstants.AUTHOR_FIELD_SEARCH, authorsSort);
	}

}
