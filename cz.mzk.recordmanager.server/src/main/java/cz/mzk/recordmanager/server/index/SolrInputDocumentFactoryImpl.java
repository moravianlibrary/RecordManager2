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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.enrich.DedupRecordEnricher;
import cz.mzk.recordmanager.server.index.enrich.HarvestedRecordEnricher;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class SolrInputDocumentFactoryImpl implements SolrInputDocumentFactory, InitializingBean {

	private static final String INSTITUTION_LIBRARY = "Library";
	private static final String INSTITUTION_OTHERS = "Others";

	private static Logger logger = LoggerFactory.getLogger(SolrInputDocumentFactoryImpl.class);
	
	private static final Pattern OAI_RECORD_ID_PATTERN = Pattern.compile("oai:[\\w|.]+:([\\w|-]+)");
	private static final Pattern RECORDTYPE_PATTERN = Pattern.compile("^(AUDIO|VIDEO|OTHER)_(.*)$");

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
	private MappingResolver propertyResolver;
	
	@Autowired
	private List<DedupRecordEnricher> dedupRecordEnrichers;

	@Autowired
	private List<HarvestedRecordEnricher> harvestedRecordEnrichers;

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Override
	public SolrInputDocument create(HarvestedRecord record) {
		try {
			Map<String, Object> fields = mapper.map(record);
			String id = getId(record);
			updateHoldings(id, fields);
			SolrInputDocument document = asSolrDocument(fields);
			if (!document.containsKey(SolrFieldConstants.ID_FIELD)) {
				document.addField(SolrFieldConstants.ID_FIELD, id);
			}
			
			harvestedRecordEnrichers.forEach(enricher -> enricher.enrich(record, document));
			
			document.addField(SolrFieldConstants.LOCAL_INSTITUTION_FIELD, getInstitution(record));
			document.addField(SolrFieldConstants.MERGED_CHILD_FIELD, 1);
			document.addField(SolrFieldConstants.WEIGHT, record.getWeight());
			document.addField(SolrFieldConstants.RECORD_FORMAT_DISPLAY, getRecordType(record));
			
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
		List<String> localIds = childs.stream().map(rec -> (String) rec.getFieldValue("id")).collect(Collectors.toCollection(ArrayList::new));
		mergedDocument.addField(SolrFieldConstants.LOCAL_IDS_FIELD, localIds);
		
		Set<String> institutions = records.stream().map(rec -> getInstitution(rec)).flatMap(it -> it.stream()).collect(Collectors.toCollection(HashSet::new));
		mergedDocument.addField(SolrFieldConstants.INSTITUTION_FIELD, institutions);
		mergedDocument.addField(SolrFieldConstants.RECORD_FORMAT, getRecordType(record));
		
		dedupRecordEnrichers.forEach(enricher -> enricher.enrich(dedupRecord, mergedDocument, childs));
		mergedDocument.addChildDocuments(childs);
		
		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return Collections.singletonList(mergedDocument);
	}

	@SuppressWarnings("unchecked")
	protected void updateHoldings(String id, Map<String, Object> fields) {
		List<String> holdings = (List<String>) fields.get(SolrFieldConstants.HOLDINGS_996_FIELD);
		if (holdings != null) {
			List<String> updatedHoldings = new ArrayList<>();
			for (String oldHolding: holdings) {
				updatedHoldings.add(oldHolding + "$z" + id);
				}
			fields.put(SolrFieldConstants.HOLDINGS_996_FIELD, updatedHoldings);
		}
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

	protected String getId(HarvestedRecord record) {
		String prefix = record.getHarvestedFrom().getIdPrefix();
		String suffix = record.getUniqueId().getRecordId();
		Matcher matcher = OAI_RECORD_ID_PATTERN.matcher(suffix);
		if (matcher.matches()) {
			suffix = matcher.group(1);
		}
		String id = ((prefix != null) ? prefix + "." : "") + suffix;
		return id;
	}

	protected String getInstitutionOfRecord(HarvestedRecord hr) {
		ImportConfiguration config = hr.getHarvestedFrom();
		if (config != null
				&& config.getLibrary() != null
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}
	
	protected String getCityOfRecord(HarvestedRecord hr) {
		ImportConfiguration config = hr.getHarvestedFrom();
		if (config != null
				&& config.getLibrary() != null
				&& config.getLibrary().getCity() != null) {
			return config.getLibrary().getCity();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}
	
	protected List<String> getInstitution(HarvestedRecord record){
		if(record.getHarvestedFrom() != null) {
			if (record.getHarvestedFrom().isLibrary()) {
				String city = MetadataUtils.normalize(getCityOfRecord(record));
				String name = getInstitutionOfRecord(record);
				return SolrUtils.createHierarchicFacetValues(INSTITUTION_LIBRARY, city, name);
			}
			else {
				String name = getInstitutionOfRecord(record);
				return SolrUtils.createHierarchicFacetValues(INSTITUTION_OTHERS, name);
			}
		}
		
		return SolrUtils.createHierarchicFacetValues(SolrFieldConstants.UNKNOWN_INSTITUTION);
		
	}

	protected List<String> getRecordType(HarvestedRecord record){
		MetadataRecord metadata = metadataFactory.getMetadataRecord(record);
		
		List<String> result = new ArrayList<String>();
		for (HarvestedRecordFormatEnum format: metadata.getDetectedFormatList()) {
			Matcher matcher = RECORDTYPE_PATTERN.matcher(format.name());
			if (matcher.matches()) {
				result.addAll(SolrUtils.createHierarchicFacetValues(matcher.group(1), matcher.group(2)));
			}
			else {
				result.addAll(SolrUtils.createHierarchicFacetValues(format.name()));
			}
		}
		return result;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (String field : fieldsWithDash) {
			String fName = field.replace('-', '_');
			remappedFields.put(fName, field);
		}
	}

}
