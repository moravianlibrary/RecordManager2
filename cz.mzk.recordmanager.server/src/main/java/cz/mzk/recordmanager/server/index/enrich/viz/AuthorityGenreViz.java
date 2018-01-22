package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class AuthorityGenreViz extends AbstractAuthorityVizFields implements
		VizFieldsMapper {

	private static final String SOURCE = Constants.PREFIX_AUTH;

	private static HashMap<String, String> GENRE_MAP = new HashMap<>();

	static {
		GENRE_MAP.put("655", "455");
	}

	@Override
	public List<String> getSuppoprtedSources() {
		return GENRE_MAP.keySet().stream().map(key -> SOURCE + key)
				.collect(Collectors.toList());
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = SPLITTER.split(value);
		String enrichField = GENRE_MAP.get(split[1]);

		List<String> results = super.getEnrichingValues(split[2], enrichField);

		super.enrichSolrField(document, SolrFieldConstants.GENRE_VIZ_FIELD, results);
	}

}
