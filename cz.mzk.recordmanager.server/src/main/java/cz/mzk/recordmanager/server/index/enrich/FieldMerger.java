package cz.mzk.recordmanager.server.index.enrich;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

public class FieldMerger {

	private final List<String> fields;

	public FieldMerger(String field) {
		super();
		this.fields = Collections.singletonList(field);
	}

	public FieldMerger(String... fields) {
		super();
		this.fields = Arrays.asList(fields);
	}

	public void mergeAndRemoveFromSources(List<SolrInputDocument> source,
			SolrInputDocument target) {
		merge(source, target);
		for (String field : fields) {
			source.stream().forEach(doc -> doc.remove(field));
		}
	}

	public void merge(List<SolrInputDocument> source,
			SolrInputDocument target) {
		for (String field : fields) {
			Set<Object> values = new HashSet<>();
			source.stream()
					.map(rec -> rec.getFieldValues(field))
					.filter(rec -> rec != null)
					.forEach(rec -> values.addAll(rec));
			target.setField(field, values);
		}
	}

}
