package cz.mzk.recordmanager.server.enrich;

import org.apache.solr.common.SolrInputDocument;

public class EnricherUtils {

	public static SolrInputDocument createDocument(String field, String... value) {
		org.apache.solr.common.SolrInputDocument doc = new SolrInputDocument();
		doc.addField(field, value);
		return doc;
	}
}
