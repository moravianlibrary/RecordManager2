package cz.mzk.recordmanager.server.index.enrich.viz;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public abstract class AbstractAuthorityVizFields {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO hrdao;

	protected static final Pattern SPLITTER = Pattern.compile("\\|");

	protected final int LRU_CACHE_SIZE_FIELD_400 = 50000;
	protected final int LRU_CACHE_SIZE_FIELD_410 = 30000;
	protected final int LRU_CACHE_SIZE_FIELD_411 = 5000;
	protected final int LRU_CACHE_SIZE_FIELD_448 = 200;
	protected final int LRU_CACHE_SIZE_FIELD_450 = 10000;
	protected final int LRU_CACHE_SIZE_FIELD_451 = 5000;
	protected final int LRU_CACHE_SIZE_FIELD_455 = 300;

	protected final Map<String, List<String>> field400Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_400));
	protected final Map<String, List<String>> field410Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_410));
	protected final Map<String, List<String>> field411Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_411));
	protected final Map<String, List<String>> field448Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_448));
	protected final Map<String, List<String>> field450Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_450));
	protected final Map<String, List<String>> field451Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_451));
	protected final Map<String, List<String>> field455Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_455));

	protected static Map<String, Map<String, List<String>>> cacheMap = new HashMap<>();
	{
		cacheMap.put("400", field400Cache);
		cacheMap.put("410", field410Cache);
		cacheMap.put("411", field411Cache);
		cacheMap.put("448", field448Cache);
		cacheMap.put("450", field450Cache);
		cacheMap.put("451", field451Cache);
		cacheMap.put("455", field455Cache);
	}

	protected List<String> getEnrichingValues(String key, String enrichingField) {
		Map<String, List<String>> cache = cacheMap.get(enrichingField);
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			HarvestedRecord hr = hrdao.findByHarvestConfAndRaw001Id(400L, key);
			if (hr != null) {
				MarcRecord mr = marcXmlParser
						.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
				List<String> results = new ArrayList<>();
				String value = mr.getField(enrichingField, 'a', 'b', 'c', 'd');
				if (value != null && !value.isEmpty()) {
					results.add(value);
					cache.put(key, results);
				}
				return results;
			}
		}
		return null;
	}

	protected void enrichSolrField(SolrInputDocument document,
			String solrField, List<String> newValues) {
		if (newValues == null || newValues.isEmpty()) {
			return;
		}
		if (document.containsKey(solrField)) {
			Collection<Object> results = document.remove(solrField).getValues();
			results.addAll(newValues);
			document.addField(solrField, results);
		} else {
			document.addField(solrField, newValues);
		}
	}

}
