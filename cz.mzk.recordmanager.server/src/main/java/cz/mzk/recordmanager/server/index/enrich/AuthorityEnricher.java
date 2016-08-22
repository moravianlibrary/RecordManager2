package cz.mzk.recordmanager.server.index.enrich;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

public abstract class AuthorityEnricher {

	@Autowired
	private HarvestedRecordDAO hrDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	private static final Pattern SOURCE_FIELD_AUTH_KEY = Pattern.compile("([0-9]{3}):(.*)");
	
	private final int LRU_CACHE_SIZE_FIELD_400 = 50000;
	private final int LRU_CACHE_SIZE_FIELD_410 = 30000;
	private final int LRU_CACHE_SIZE_FIELD_411 = 5000;
	private final int LRU_CACHE_SIZE_FIELD_448 = 200;
	private final int LRU_CACHE_SIZE_FIELD_450 = 10000;
	private final int LRU_CACHE_SIZE_FIELD_451 = 5000;
	private final int LRU_CACHE_SIZE_FIELD_455 = 300;
	
	private final Map<String, String> field400Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_400));
	private final Map<String, String> field410Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_410));
	private final Map<String, String> field411Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_411));
	private final Map<String, String> field448Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_448));
	private final Map<String, String> field450Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_450));
	private final Map<String, String> field451Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_451));
	private final Map<String, String> field455Cache = Collections.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_455));
	
	private static Map<String, Map<String, String>> cacheMap = new HashMap<>();
	{
		cacheMap.put("400", field400Cache);
		cacheMap.put("410", field410Cache);
		cacheMap.put("411", field411Cache);
		cacheMap.put("448", field448Cache);
		cacheMap.put("450", field450Cache);
		cacheMap.put("451", field451Cache);
		cacheMap.put("455", field455Cache);
	}

	protected Set<String> getSolrField(List<SolrInputDocument> localRecords, String dummyField, Map<String, String> sourceAuthFieldMap){
		Set<Object> authorityKeys = new HashSet<Object>();
		
		// extract all authority keys from local records
		localRecords.stream()
			.filter(r -> r.containsKey(dummyField)) //
			.forEach(r -> r.getFieldValues(dummyField) //
				.stream().filter(s -> (s instanceof String)).forEach(s -> authorityKeys.add((String) s)) //
			);
		
		Set<String> result = new HashSet<>();
		String currentAuthFieldTag = null;
		String currentAuthKey = null;
		Map<String, String> currentCache = null;
		for (Object sourceFieldAndAuthKey: authorityKeys) {
			if (!(sourceFieldAndAuthKey instanceof String)) {
				continue;
			}
			String sourceFieldAndAuthKeyStr = (String) sourceFieldAndAuthKey;
			Matcher matcher = SOURCE_FIELD_AUTH_KEY.matcher(sourceFieldAndAuthKeyStr);
			if(matcher.matches() && sourceAuthFieldMap.containsKey(matcher.group(1))){
				currentAuthFieldTag = sourceAuthFieldMap.get(matcher.group(1));
				currentAuthKey = matcher.group(2);
				currentCache = cacheMap.get(currentAuthFieldTag);
				if(currentCache == null) continue;
			}
			else continue;
			
			// check cache for field value
			if (currentCache.containsKey(currentAuthKey)) {
				result.add(currentCache.get(currentAuthKey));
			} else {
				// parse value from authority record
				HarvestedRecord auth = hrDao.findByHarvestConfAndRaw001Id(Constants.IMPORT_CONF_ID_AUTHORITY, currentAuthKey);
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
				
				String resultAuthStr = marc.getField(currentAuthFieldTag, 'a', 'b', 'c', 'd');
				if (resultAuthStr != null && !resultAuthStr.isEmpty()) {
					result.add(resultAuthStr);
					currentCache.put(auth.getRaw001Id(), resultAuthStr);
				}
			}
		}
		
		return result;
	}
}
