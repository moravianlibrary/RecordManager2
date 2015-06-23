package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.marc.MarcDSL;

@Component
public class SolrInputDocumentFactoryImpl implements SolrInputDocumentFactory, InitializingBean {

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

	public SolrInputDocument create(DedupRecord dedupRecord, List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		HarvestedRecord record = records.get(0);
		SolrInputDocument document = parse(record);
		document.addField(SolrFieldConstants.ID_FIELD, dedupRecord.getId());
		document.addField(SolrFieldConstants.INSTITUTION_FIELD, getInstitution(record));
		document.addField(SolrFieldConstants.MERGED_FIELD, 1);
		document.addField(SolrFieldConstants.WEIGHT, records.get(0).getWeight());
		document.addField(SolrFieldConstants.CITY_INSTITUTION_CS, getCityInstitutionForSearching(record));
		List<String> localIds = new ArrayList<String>();
		for (HarvestedRecord rec : records) {
			localIds.add(getId(rec));
		}
		document.addField(SolrFieldConstants.LOCAL_IDS_FIELD, localIds);
		if (logger.isTraceEnabled()) {
			logger.info("Mapping of dedupRecord with id = {} finished", dedupRecord.getId());
		}
		return document;
	}

	protected SolrInputDocument parse(HarvestedRecord record) {
		Map<String, Object> fields = mapper.map(record);
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
		OAIHarvestConfiguration config = hr.getHarvestedFrom();
		if (config != null
				&& config.getLibrary() != null
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}
	
	protected String getCityOfRecord(HarvestedRecord hr) {
		OAIHarvestConfiguration config = hr.getHarvestedFrom();
		if (config != null
				&& config.getLibrary() != null
				&& config.getLibrary().getCity() != null) {
			return config.getLibrary().getCity();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}
	
	protected List<String> getInstitution(HarvestedRecord record){
		List<String> result = new ArrayList<String>();
		String city = getCityOfRecord(record);
		String name = getInstitutionOfRecord(record);
		result.add("0/"+city+"/");
		result.add("1/"+city+"/"+name+"/");

		return result;
	}
	
	protected List<String> getCityInstitutionForSearching(HarvestedRecord hr){
		List<String> result = new ArrayList<String>();
		result.add(getCityOfRecord(hr));
		MarcDSL marcdsl = new MarcDSL(propertyResolver);
		try{
			result.add(marcdsl.translate("mzk_institution.map", getInstitutionOfRecord(hr), null));
		}catch(Exception ex){}
		
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
