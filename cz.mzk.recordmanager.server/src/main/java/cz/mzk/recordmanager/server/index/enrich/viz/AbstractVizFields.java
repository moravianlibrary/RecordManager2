package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

public abstract class AbstractVizFields {

	protected abstract List<String> getEnrichingValues(String key, String enrichingField);

	protected void enrichSolrField(SolrInputDocument document,
			String solrField, List<String> newValues) {
		if (newValues == null || newValues.isEmpty()) {
			return;
		}
		if (document.containsKey(solrField)) {
			Collection<Object> results = document.remove(solrField).getValues();
			results.addAll(newValues);
			document.addField(solrField, results);
		} else {
			document.addField(solrField, newValues);
		}
	}

}
