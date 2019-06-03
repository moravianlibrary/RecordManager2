package cz.mzk.recordmanager.server.index.enrich.viz;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorityUniftitleViz extends AbstractAuthorityVizFields implements
		VizFieldsMapper {

	private static final String SOURCE = Constants.PREFIX_AUTH;

	private static HashMap<String, String> UNIFTITLE_MAP = new HashMap<>();

	static {
		UNIFTITLE_MAP.put("130", "430");
		UNIFTITLE_MAP.put("630", "430");
		UNIFTITLE_MAP.put("730", "430");
	}

	@Override
	public List<String> getSuppoprtedSources() {
		return UNIFTITLE_MAP.keySet().stream().map(key -> SOURCE + key)
				.collect(Collectors.toList());
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = SPLITTER.split(value);
		String enrichField = UNIFTITLE_MAP.get(split[1]);

		List<String> results = super.getEnrichingValues(split[2], enrichField);

		super.enrichSolrField(document, SolrFieldConstants.UNIFTITLE_VIZ_FIELD, results);
	}

}
