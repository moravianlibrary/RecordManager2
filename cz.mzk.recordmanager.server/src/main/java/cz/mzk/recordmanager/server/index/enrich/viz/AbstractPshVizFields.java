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
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public abstract class AbstractPshVizFields {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO hrdao;

	protected static final Pattern SPLITTER = Pattern.compile("\\|");

	private final int LRU_CACHE_SIZE_FIELD_450 = 10000;

	private final Map<String, List<String>> field450Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_450));

	private static Map<String, Map<String, List<String>>> cacheMap = new HashMap<>();
	{
		cacheMap.put("450", field450Cache);
	}

	protected List<String> getEnrichingValues(String key, String enrichingField) {
		Map<String, List<String>> cache = cacheMap.get(enrichingField);
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			HarvestedRecord hr = hrdao.findByHarvestConfAndTezaurus(351L, key);
			if (hr != null) {
				MarcRecord mr = marcXmlParser
						.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
				List<String> results = new ArrayList<>();
				for (DataField df : mr.getDataFields(enrichingField)) {
					if (df.getSubfield('a') != null) {
						results.add(df.getSubfield('a').getData());
					}
				}
				cache.put(key, results);
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
			System.out.println(document.getFieldNames());
			Collection<Object> results = document.remove(solrField).getValues();
			System.out.println(results);
			results.addAll(newValues);
			document.addField(solrField, results);
		}
		else {
			document.addField(solrField, newValues);
		}
	}

}
