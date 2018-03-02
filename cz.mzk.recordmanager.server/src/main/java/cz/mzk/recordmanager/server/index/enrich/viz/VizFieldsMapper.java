package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;

public interface VizFieldsMapper {

	List<String> getSuppoprtedSources();

	default void parse(List<String> values, SolrInputDocument document) {
		for (String value : values) {
			parse(value, document);
		}
	}

	void parse(String value, SolrInputDocument document);

}
