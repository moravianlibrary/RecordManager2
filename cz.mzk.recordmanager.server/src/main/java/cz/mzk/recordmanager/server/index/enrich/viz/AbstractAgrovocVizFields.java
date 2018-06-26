package cz.mzk.recordmanager.server.index.enrich.viz;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LRUMap;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.TezaurusRecord;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.TezaurusDAO;
import cz.mzk.recordmanager.server.util.Constants;

public abstract class AbstractAgrovocVizFields extends AbstractVizFields {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private TezaurusDAO tezaurusDao;

	@Autowired
	private ImportConfigurationDAO configDao;

	private static ImportConfiguration config = null;

	protected static final Pattern SPLITTER = Pattern.compile("\\|");

	private static final int LRU_CACHE_SIZE_FIELD_450 = 6000;

	private final Map<String, List<String>> field450Cache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_450));

	private static Map<String, Map<String, List<String>>> cacheMap = new HashMap<>();
	{
		cacheMap.put("450", field450Cache);
	}

	@Override
	protected List<String> getEnrichingValues(String key, String enrichingField) {
		if (config == null) {
			List<ImportConfiguration> configs = configDao.findByIdPrefix(Constants.PREFIX_AGROVOC);
			if (configs != null && !configs.isEmpty()) {
				config = configs.get(0);
			}
		}
		Map<String, List<String>> cache = cacheMap.get(enrichingField);
		if (cache.containsKey(key)) {
			return new ArrayList<>(cache.get(key));
		} else {
			TezaurusRecord tr = tezaurusDao.findByConfigAndSourceFieldAndName(
					config, '1' + enrichingField.substring(1), key);
			if (tr != null) {
				MarcRecord mr = marcXmlParser.parseRecord(new ByteArrayInputStream(tr.getRawRecord()));
				List<String> results = new ArrayList<>();
				for (DataField df : mr.getDataFields(enrichingField)) {
					if (df.getSubfield('a') != null) {
						results.add(df.getSubfield('a').getData());
					}
				}
				if (!results.isEmpty()) {
					results = Collections.unmodifiableList(results);
					cache.put(key, results);
					return new ArrayList<>(results);
				}
			}
		}
		return null;
	}

}
