package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.model.CaslinLinks;
import cz.mzk.recordmanager.server.oai.dao.CaslinLinksDAO;
import org.apache.commons.collections4.map.LRUMap;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class CaslinLink {

	@Autowired
	private CaslinLinksDAO caslinLinksDAO;

	private static final int LRU_CACHE_SIZE_FIELD_SIGLA = 1000;

	private final Map<String, CaslinLinks> siglaCache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_SIGLA));

	private static final Pattern CARMEN = Pattern.compile("carmen", Pattern.CASE_INSENSITIVE);

	public String getCaslinLink(DataField df) {
		if (df.getSubfield('e') == null || df.getSubfield('w') == null) return "";
		return getCaslinLink(df.getSubfield('e').getData(), df.getSubfield('w').getData());
	}

	public String getCaslinLink(String sigla, String id) {
		CaslinLinks caslinLink;
		if (siglaCache.containsKey(sigla)) {
			caslinLink = siglaCache.get(sigla);
		} else {
			caslinLink = caslinLinksDAO.getBySigla(sigla);
			siglaCache.put(sigla, caslinLink);
		}
		if (caslinLink == null) return "";
		if (CARMEN.matcher(caslinLink.getUrlForIndexing()).find() && id.length() >= 8)
			id = id.substring(id.length() - 8);
		return caslinLink.getUrlForIndexing() + id;
	}

}
