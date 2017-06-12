package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class AuthorityAuthorViz extends AbstractAuthorityVizFields implements
		VizFieldsMapper {

	private static final String SOURCE = Constants.PREFIX_AUTH;

	private static HashMap<String, String> AUTHOR_MAP = new HashMap<>();
	{
		AUTHOR_MAP.put("100", "400");
		AUTHOR_MAP.put("700", "400");
	}

	@Override
	public List<String> getSuppoprtedSources() {
		return AUTHOR_MAP.keySet().stream().map(key -> SOURCE + key)
				.collect(Collectors.toList());
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = SPLITTER.split(value);
		String enrichField = AUTHOR_MAP.get(split[1]);

		List<String> results = super.getEnrichingValues(split[2], enrichField);

		super.enrichSolrField(document, SolrFieldConstants.AUTHOR_VIZ_FIELD, results);
	}

}
