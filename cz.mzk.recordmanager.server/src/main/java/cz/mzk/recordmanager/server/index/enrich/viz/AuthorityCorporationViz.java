package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class AuthorityCorporationViz extends AbstractAuthorityVizFields
		implements VizFieldsMapper {

	private static final String SOURCE = Constants.PREFIX_AUTH;

	private static HashMap<String, String> CORPORATION_MAP = new HashMap<>();
	{
		CORPORATION_MAP.put("110", "410");
		CORPORATION_MAP.put("111", "411");
		CORPORATION_MAP.put("710", "410");
		CORPORATION_MAP.put("711", "411");
	}

	@Override
	public List<String> getSuppoprtedSources() {
		return CORPORATION_MAP.keySet().stream().map(key -> SOURCE + key)
				.collect(Collectors.toList());
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = SPLITTER.split(value);
		String enrichField = CORPORATION_MAP.get(split[1]);

		List<String> results = super.getEnrichingValues(split[2], enrichField);

		super.enrichSolrField(document, SolrFieldConstants.CORPORATION_VIZ_FIELD, results);
	}

}
