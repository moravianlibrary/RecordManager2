package cz.mzk.recordmanager.server.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.AntikvariatyRecordDAO;
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
	private AntikvariatyRecordDAO antikvariatyRecordDao;

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

	@SuppressWarnings("unchecked")
	public List<SolrInputDocument> create(DedupRecord dedupRecord, List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		
		List<SolrInputDocument> documentList = records.stream().map(rec -> create(rec)).collect(Collectors.toCollection(ArrayList::new));
		
		HarvestedRecord record = records.get(0);
		SolrInputDocument mergedDocument = parse(record);
		mergedDocument.addField(SolrFieldConstants.ID_FIELD, dedupRecord.getId());
		mergedDocument.addField(SolrFieldConstants.INSTITUTION_FIELD, getInstitution(record));
		mergedDocument.addField(SolrFieldConstants.MERGED_FIELD, 1);
		mergedDocument.addField(SolrFieldConstants.WEIGHT, record.getWeight());
		mergedDocument.addField(SolrFieldConstants.CITY_INSTITUTION_CS, getCityInstitutionForSearching(record));
		mergedDocument.addField(SolrFieldConstants.EXTERNAL_LINKS_FIELD, getExternalLinks(dedupRecord));
		
		List<String> localIds = records.stream().map(rec -> getId(record)).collect(Collectors.toCollection(ArrayList::new));
		mergedDocument.addField(SolrFieldConstants.LOCAL_IDS_FIELD, localIds);
		
		// merge holdings from all records
		List<String> allHoldings996 = new ArrayList<>();
		documentList.stream() //
			.map(doc -> doc.getField(SolrFieldConstants.HOLDINGS_996_FIELD)) //
			.filter(field -> field != null && field.getValue() != null) //
			.forEach(field -> allHoldings996.addAll((List<String>) field.getValue()));
		mergedDocument.remove(SolrFieldConstants.HOLDINGS_996_FIELD);
		mergedDocument.addField(SolrFieldConstants.HOLDINGS_996_FIELD, allHoldings996);
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
	
	protected List<String> getExternalLinks(DedupRecord dr) {
		List<String> result = new ArrayList<>();
		
		String antikvariatyURL = antikvariatyRecordDao.getLinkToAntikvariaty(dr);
		if (antikvariatyURL != null) {
			result.add("antikvariaty:" + antikvariatyURL);
		}
		
		return result;
	}

}
