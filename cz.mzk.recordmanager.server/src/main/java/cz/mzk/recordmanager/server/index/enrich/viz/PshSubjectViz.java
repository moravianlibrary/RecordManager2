package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class PshSubjectViz extends AbstractPshVizFields implements
		VizFieldsMapper {

	private static final String SOURCE = Constants.PREFIX_PSH;

	private static HashMap<String, String> SUBJECT_MAP = new HashMap<>();
	{
		SUBJECT_MAP.put("650", "450");
		SUBJECT_MAP.put("699", "450");
	}

	@Override
	public List<String> getSuppoprtedSources() {
		return SUBJECT_MAP.keySet().stream().map(key -> SOURCE + key)
				.collect(Collectors.toList());
	}

	@Override
	public void parse(String value, SolrInputDocument document) {
		String[] split = SPLITTER.split(value);
		String enrichField = SUBJECT_MAP.get(split[1]);

		List<String> results = super.getEnrichingValues(split[2], enrichField);
		
		super.enrichSolrField(document, SolrFieldConstants.SUBJECT_VIZ_FIELD, results);
	}

}
