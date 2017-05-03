package cz.mzk.recordmanager.server.index.enrich.viz;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LRUMap;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public abstract class AbstractPshVizFields extends AbstractVizFields {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private HarvestedRecordDAO hrdao;

	protected static final Pattern SPLITTER = Pattern.compile("\\|");

	private static final int LRU_CACHE_SIZE_FIELD_450 = 10000;

	private final Map<String, String> field450Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_450));

	private static Map<String, Map<String, String>> cacheMap = new HashMap<>();
	{
		cacheMap.put("450", field450Cache);
	}

	@Override
	protected String getEnrichingValues(String key, String enrichingField) {
		Map<String, String> cache = cacheMap.get(enrichingField);
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else {
			HarvestedRecord hr = hrdao.findByHarvestConfAndTezaurus(351L, key);
			if (hr != null) {
				MarcRecord mr = marcXmlParser
						.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
				for (DataField df : mr.getDataFields(enrichingField)) {
					if (df.getSubfield('a') != null) {
						cache.put(key, df.getSubfield('a').getData());
						return df.getSubfield('a').getData();
					}
				}
			}
		}
		return null;
	}

}
