package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.scripting.MarcMappingScript;
import cz.mzk.recordmanager.server.scripting.MarcScriptFactory;

@Component
public class MarcSolrRecordMapper implements SolrRecordMapper, InitializingBean {

	private static final String ID_FIELD = "id";
	
	private static final String INSTITUTION_FIELD = "institution";
	
	private static final String LOCAL_IDS_FIELD = "local_ids_str_mv";
	
	private static final String MERGED_FIELD = "merged_boolean";
	
	private static final String MERGED_CHILD_FIELD = "merged_child_boolean";
	
	private static final String UNKNOWN_INSTITUTION = "unknown";

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private MarcScriptFactory marcScriptFactory;

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

	private MarcMappingScript mappingScript;

	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		HarvestedRecord record = records.get(0);
		SolrInputDocument document = parse(record);
		document.addField(ID_FIELD, dedupRecord.getId());
		document.addField(INSTITUTION_FIELD, getInstituitonOfRecord(record));
		document.addField(MERGED_FIELD, 1);
		List<String> localIds = new ArrayList<String>();
		for (HarvestedRecord rec : records) {
			localIds.add(getId(rec));
		}
		document.addField(LOCAL_IDS_FIELD, localIds);
		return document;
	}
	
	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrInputDocument document = parse(record);
		String id = getId(record); 
		document.addField(ID_FIELD, id);
		document.addField(INSTITUTION_FIELD, getInstituitonOfRecord(record));
		document.addField(MERGED_CHILD_FIELD, 1);
		return document;
	}

	protected SolrInputDocument parse(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		SolrInputDocument document = new SolrInputDocument();
		MarcRecord rec = marcXmlParser.parseRecord(is);
		MarcMappingScript script = getMappingScript(record);
		Map<String, Object> fields = script.parse(rec);
		for (Entry<String, Object> field : fields.entrySet()) {
			String fName = remappedFields.getOrDefault(field.getKey(),
					field.getKey());
			Object fValue = field.getValue();
			document.addField(fName, fValue);
		}
		return document;
	}

	protected MarcMappingScript getMappingScript(HarvestedRecord record) {
		return mappingScript;
	}

	protected String getId(HarvestedRecord record) {
		String prefix = record.getHarvestedFrom().getIdPrefix();
		String id = ((prefix != null) ? prefix + "." : "") + record.getUniqueId().getRecordId();
		return id;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		for (String field : fieldsWithDash) {
			String fName = field.replace('-', '_');
			remappedFields.put(fName, field);
		}
		mappingScript = marcScriptFactory.create(getClass()
				.getResourceAsStream("/marc/groovy/BaseMarc.groovy"));
	}
	
	protected String getInstituitonOfRecord(HarvestedRecord hr) {
		OAIHarvestConfiguration config = hr.getHarvestedFrom();
		if (config != null 
				&& config.getLibrary() != null 
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return UNKNOWN_INSTITUTION;
	}

}
