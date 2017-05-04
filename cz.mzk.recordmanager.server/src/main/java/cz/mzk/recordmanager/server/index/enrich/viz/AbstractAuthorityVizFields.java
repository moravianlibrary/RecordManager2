package cz.mzk.recordmanager.server.index.enrich.viz;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LRUMap;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public abstract class AbstractAuthorityVizFields extends AbstractVizFields {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO hrdao;

	protected static final Pattern SPLITTER = Pattern.compile("\\|");

	private static final int LRU_CACHE_SIZE_FIELD_400 = 50000;
	private static final int LRU_CACHE_SIZE_FIELD_410 = 30000;
	private static final int LRU_CACHE_SIZE_FIELD_411 = 5000;
	private static final int LRU_CACHE_SIZE_FIELD_448 = 200;
	private static final int LRU_CACHE_SIZE_FIELD_450 = 10000;
	private static final int LRU_CACHE_SIZE_FIELD_451 = 5000;
	private static final int LRU_CACHE_SIZE_FIELD_455 = 300;

	private final Map<String, List<String>> field400Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_400));
	private final Map<String, List<String>> field410Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_410));
	private final Map<String, List<String>> field411Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_411));
	private final Map<String, List<String>> field448Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_448));
	private final Map<String, List<String>> field450Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_450));
	private final Map<String, List<String>> field451Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_451));
	private final Map<String, List<String>> field455Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_455));

	private static Map<String, Map<String, List<String>>> cacheMap = new HashMap<>();
	{
		cacheMap.put("400", field400Cache);
		cacheMap.put("410", field410Cache);
		cacheMap.put("411", field411Cache);
		cacheMap.put("448", field448Cache);
		cacheMap.put("450", field450Cache);
		cacheMap.put("451", field451Cache);
		cacheMap.put("455", field455Cache);
	}

	@Override
	protected List<String> getEnrichingValues(String key, String enrichingField) {
		Map<String, List<String>> cache = cacheMap.get(enrichingField);
		if (cache.containsKey(key)) {
			return new ArrayList<>(cache.get(key));
		} else {
			HarvestedRecord hr = hrdao.findByHarvestConfAndRaw001Id(400L, key);
			if (hr != null) {
				MarcRecord mr = marcXmlParser
						.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
				List<String> results = new ArrayList<>(mr.getFields(
						enrichingField, "", 'a', 'b', 'c', 'd'));
				if (!results.isEmpty()) {
					cache.put(key, Collections.unmodifiableList(results));
					return results;
				}
			}
		}
		return null;
	}

}
