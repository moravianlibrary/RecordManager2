package cz.mzk.recordmanager.server.index.enrich;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

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

	public void renameField(SolrInputDocument source, String oldName, String newName) {
		SolrInputField field = source.remove(oldName);
		if (field != null) {
			source.addField(newName, field);
		}
	}

	public void copyField(SolrInputDocument doc, String sourceName, String targetName) {
		if (doc.containsKey(sourceName)) doc.addField(targetName, doc.getField(sourceName));
	}

}
