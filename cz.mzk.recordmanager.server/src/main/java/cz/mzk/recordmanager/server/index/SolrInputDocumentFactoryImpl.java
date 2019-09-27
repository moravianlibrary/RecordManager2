package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.enrich.DedupRecordEnricher;
import cz.mzk.recordmanager.server.index.enrich.HarvestedRecordEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.util.IndexingUtils;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class SolrInputDocumentFactoryImpl implements SolrInputDocumentFactory, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(SolrInputDocumentFactoryImpl.class);

	private List<String> fieldsWithDash = Arrays.asList( //
			"author2-role", //
			"author-letter", //
			"callnumber-a", //
			"callnumber-first", //
			"callnumber-first-code", //
			"callnumber-subject", //
			"callnumber-subject-code", //
			"callnumber-label", //
			"dewey-hundreds", //
			"dewey-tens", //
			"dewey-ones", //
			"dewey-full", //
			"dewey-sort", //
			"dewey-sort-browse", //
			"dewey-raw" //
	);

	private final Map<String, String> remappedFields = new HashMap<String, String>();
		
	@Autowired
	private DelegatingSolrRecordMapper mapper;

	@Autowired
	private List<DedupRecordEnricher> dedupRecordEnrichers;

	@Autowired
	private List<HarvestedRecordEnricher> harvestedRecordEnrichers;

	@Override
	public SolrInputDocument create(HarvestedRecord record) {
		try {
			Map<String, Object> fields = mapper.map(record);
			String id = IndexingUtils.getSolrId(record);
			SolrInputDocument document = asSolrDocument(fields);
			if (!document.containsKey(SolrFieldConstants.ID_FIELD)) {
				document.addField(SolrFieldConstants.ID_FIELD, id);
			}
			
			harvestedRecordEnrichers.forEach(enricher -> enricher.enrich(record, document));
			document.addField(SolrFieldConstants.MERGED_CHILD_FIELD, 1);
			document.addField(SolrFieldConstants.WEIGHT, record.getWeight());
	
			return document;
		} catch (Exception ex) {
			logger.error(String.format("Exception thrown when indexing dedup_record with id=%s", record.getUniqueId()), ex);
			return null;
		}
	}

	public List<SolrInputDocument> create(DedupRecord dedupRecord, List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}

		List<SolrInputDocument> childs = records.stream().map(rec -> create(rec)).collect(Collectors.toCollection(ArrayList::new));
		SolrUtils.sortByWeight(childs);
		
		HarvestedRecord record = records.get(0);
		SolrInputDocument mergedDocument = asSolrDocument(mapper.map(dedupRecord, records));
		mergedDocument.addField(SolrFieldConstants.ID_FIELD, dedupRecord.getId());
		mergedDocument.addField(SolrFieldConstants.MERGED_FIELD, 1);
		mergedDocument.addField(SolrFieldConstants.WEIGHT, record.getWeight());
		mergedDocument.addField(SolrFieldConstants.LOCAL_IDS_FIELD, getLocalIds(childs));
		if(childs.size() > 1) mergedDocument.addField(SolrFieldConstants.MERGED_RECORDS, 1);
		mergedDocument.addField(SolrFieldConstants.INSPIRATION, getInspirations(records));
		
		dedupRecordEnrichers.forEach(enricher -> enricher.enrich(dedupRecord, mergedDocument, childs));
		mergedDocument.addChildDocuments(childs);
		
		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return Collections.singletonList(mergedDocument);
	}

	protected SolrInputDocument asSolrDocument(Map<String, Object> fields) {
		SolrInputDocument document = new SolrInputDocument();
		for (Entry<String, Object> field : fields.entrySet()) {
			String fName = remappedFields.getOrDefault(field.getKey(),
					field.getKey());
			Object fValue = field.getValue();
			document.addField(fName, fValue);
		}
		return document;
	}

	private static final String TOP_RESULTS = "top_results";

	protected Set<String> getInspirations(List<HarvestedRecord> records){
		Set<String> result = new HashSet<>();
		
		for(HarvestedRecord record: records){
			for(Inspiration inspiration: record.getInspiration()){
				result.add(inspiration.getName());
			}
			if (result.size() > 0) result.add(TOP_RESULTS);
		}
		
		return result;
	}

	protected List<String> getLocalIds(List<SolrInputDocument> childs) {
		List<String> index = new ArrayList<>();
		List<String> others = new ArrayList<>();
		childs.forEach(rec -> {
			if (rec.getFieldValue(SolrFieldConstants.INDEXING_WHEN_MERGED) == null
					|| (boolean) rec
							.getFieldValue(SolrFieldConstants.INDEXING_WHEN_MERGED))
				index.add((String) rec
						.getFieldValue(SolrFieldConstants.ID_FIELD));
			else
				others.add((String) rec
						.getFieldValue(SolrFieldConstants.ID_FIELD));
		});

		return (index.size() > 0) ? index : others;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (String field : fieldsWithDash) {
			String fName = field.replace('-', '_');
			remappedFields.put(fName, field);
		}
	}

}
