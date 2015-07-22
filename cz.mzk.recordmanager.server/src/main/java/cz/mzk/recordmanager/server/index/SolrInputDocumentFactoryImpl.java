package cz.mzk.recordmanager.server.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.enrich.DedupRecordEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class SolrInputDocumentFactoryImpl implements SolrInputDocumentFactory, InitializingBean {

	private static final String MZK_INSTITUTION_MAP = "mzk_institution.map";

	private static Logger logger = LoggerFactory.getLogger(SolrInputDocumentFactoryImpl.class);
	
	private static final Pattern OAI_RECORD_ID_PATTERN = Pattern.compile("oai:[\\w|.]+:([\\w|-]+)");

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

	@Override
	public SolrInputDocument create(HarvestedRecord record) {
		try {
			SolrInputDocument document = parse(record);
			if (!document.containsKey(SolrFieldConstants.ID_FIELD)) {
				String id = getId(record);
				document.addField(SolrFieldConstants.ID_FIELD, id);
			}
			document.addField(SolrFieldConstants.INSTITUTION_FIELD, getInstitutionOfRecord(record));
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

		List<SolrInputDocument> documentList = records.stream().map(rec -> create(rec)).collect(Collectors.toCollection(ArrayList::new));
		
		HarvestedRecord record = records.get(0);
		SolrInputDocument mergedDocument = parse(record);
		mergedDocument.addField(SolrFieldConstants.ID_FIELD, dedupRecord.getId());
		mergedDocument.addField(SolrFieldConstants.MERGED_FIELD, 1);
		mergedDocument.addField(SolrFieldConstants.WEIGHT, record.getWeight());
		mergedDocument.addField(SolrFieldConstants.CITY_INSTITUTION_CS, getCityInstitutionForSearching(record));
		
		List<String> localIds = records.stream().map(rec -> getId(rec)).collect(Collectors.toCollection(ArrayList::new));
		mergedDocument.addField(SolrFieldConstants.LOCAL_IDS_FIELD, localIds);
		
		Set<String> institutions = records.stream().map(rec -> getInstitution(rec)).collect(new UniqueCollector<String>());
		mergedDocument.addField(SolrFieldConstants.INSTITUTION_FIELD, institutions);
		
		dedupRecordEnrichers.forEach(enricher -> enricher.enrich(dedupRecord, mergedDocument, documentList));
		documentList.add(mergedDocument);

		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return documentList;
	}

	protected SolrInputDocument parse(HarvestedRecord record) {
		Map<String, Object> fields = mapper.map(record);
		SolrInputDocument document = new SolrInputDocument();
		for (Entry<String, Object> field : fields.entrySet()) {
			String fName = remappedFields.getOrDefault(field.getKey(),
					field.getKey());
			Object fValue = field.getValue();
			String id = getId(record);
			if (fName.equals(SolrFieldConstants.HOLDINGS_996_FIELD)) {
				//add ids to holdings_996_field
				
				@SuppressWarnings("unchecked")
				List<String> holdings = (List<String>) fValue;
				List<String> updatedHoldings = new ArrayList<>();
				for (String oldHolding: holdings) {
					updatedHoldings.add(oldHolding + "$z" + id);
				}
				fValue = updatedHoldings;
			}
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
		String city = getCityOfRecord(record);
		String name = getInstitutionOfRecord(record);
		return SolrUtils.createHierarchicFacetValues(city, name);
	}

	protected List<String> getCityInstitutionForSearching(HarvestedRecord hr){
		List<String> result = new ArrayList<String>();
		result.add(getCityOfRecord(hr));
		try {
			result.add(propertyResolver.resolve(MZK_INSTITUTION_MAP).get(getInstitutionOfRecord(hr)));
		} catch (IOException ioe){
			throw new IllegalArgumentException(
					String.format("Mapping for %s can't be open", MZK_INSTITUTION_MAP));
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
	
	protected class UniqueCollector<T> implements Collector<List<T>, Set<T>, Set<T>>{

		@Override
		public Supplier<Set<T>> supplier() {
			return HashSet::new;
		}

		@Override
		public BiConsumer<Set<T>, List<T>> accumulator() {
			return (accum, input) -> input.forEach(cur -> accum.add(cur));
		}

		@Override
		public BinaryOperator<Set<T>> combiner() {
			return (x,y) -> {x.addAll(y); return x;}; 
		}

		@Override
		public Function<Set<T>, Set<T>> finisher() {
			return accumulator -> accumulator;
		}

		@Override
		public Set<java.util.stream.Collector.Characteristics> characteristics() {
			return EnumSet.of(Characteristics.UNORDERED);
		}
		
	}

}
