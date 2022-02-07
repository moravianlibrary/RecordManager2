package cz.mzk.recordmanager.server.enrich;

import org.apache.solr.common.SolrInputDocument;

public class EnricherUtils {

	public static SolrInputDocument createDocument(String field, String... value) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(field, value);
		return doc;
	}

	public static SolrInputDocument createDocument() {
		return new SolrInputDocument();
	}

}
