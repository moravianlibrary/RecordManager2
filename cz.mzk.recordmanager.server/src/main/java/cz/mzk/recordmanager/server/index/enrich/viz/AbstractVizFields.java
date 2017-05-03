package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;

public abstract class AbstractVizFields {

	protected abstract String getEnrichingValues(String key, String enrichingField);

	protected void enrichSolrField(SolrInputDocument document,
			String solrField, String newValue) {
		if (newValue == null || newValue.isEmpty()) {
			return;
		}
		if (document.containsKey(solrField)) {
			Collection<Object> results = document.remove(solrField).getValues();
			results.add(newValue);
			document.addField(solrField, results);
		} else {
			document.addField(solrField, newValue);
		}
	}

}
