package cz.mzk.recordmanager.server.util;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.oai.dao.SiglaDAO;

@Component
public class CaslinFilter {

	@Autowired
	private SiglaDAO siglaDao;

	private final int LRU_CACHE_SIZE = 100;

	private final Map<String, Boolean> siglaCache = Collections.synchronizedMap(new LRUMap<String, Boolean>(LRU_CACHE_SIZE));

	/**
	 * is sigla in db table 'sigla'?
	 * @param sigla
	 * @return 
	 */
	public boolean filter(String sigla) {
		if (sigla == null) return false;
		if (siglaCache.containsKey(sigla)) {
			if (!siglaCache.get(sigla)) return false;
		} else {
			if (siglaDao.findSiglaByName(sigla).isEmpty()) {
				siglaCache.put(sigla, false);
				return false;
			} else {
				siglaCache.put(sigla, true);
				return true;
			}
		}
		return true;
	}
}
