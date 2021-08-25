package cz.mzk.recordmanager.server.metadata.mappings996;

import cz.mzk.recordmanager.server.model.CaslinLinks;
import cz.mzk.recordmanager.server.oai.dao.CaslinLinksDAO;
import org.apache.commons.collections4.map.LRUMap;
import org.marc4j.marc.DataField;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class CaslinMappings996 extends DefaultMappings996 {

	@Autowired
	private CaslinLinksDAO caslinLinksDAO;

	private static final int LRU_CACHE_SIZE_FIELD_SIGLA = 1000;

	private final Map<String, CaslinLinks> siglaCache = Collections
			.synchronizedMap(new LRUMap<>(LRU_CACHE_SIZE_FIELD_SIGLA));

	private static final Pattern CARMEN = Pattern.compile("carmen", Pattern.CASE_INSENSITIVE);

	@Override
	public String getDepartment(DataField df) {
		return df.getSubfield('e') != null ? df.getSubfield('e').getData() : "";
	}

	@Override
	public String getCaslinUrl(DataField df) {
		if (df.getSubfield('e') == null || df.getSubfield('w') == null) return "";
		String sigla = df.getSubfield('e').getData();
		CaslinLinks caslinLink;
		if (siglaCache.containsKey(sigla)) {
			caslinLink = siglaCache.get(sigla);
		} else {
			caslinLink = caslinLinksDAO.getBySigla(sigla);
			siglaCache.put(sigla, caslinLink);
		}
		if (caslinLink == null) return "";
		String id = df.getSubfield('w').getData();
		if (CARMEN.matcher(caslinLink.getUrl()).find() && id.length() >= 8) id = id.substring(id.length() - 8);
		return caslinLink.getUrl() + id;
	}

}
