package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import cz.mzk.recordmanager.server.scripting.MarcMappingScript;
import cz.mzk.recordmanager.server.scripting.MarcScriptFactory;

@Component
public class MarcSolrRecordMapper implements SolrRecordMapper, InitializingBean {
	
	private static final String ID_FIELD = "id";
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Autowired
	private MarcScriptFactory marcScriptFactory;
	
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
		return document;
	}
	
	protected SolrInputDocument parse(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		SolrInputDocument document = new SolrInputDocument();
		MarcRecord rec = marcXmlParser.parseRecord(is);
		MarcMappingScript script = getMappingScript(record);
		Map<String, Object> fields = script.parse(rec);
		for (Entry<String, Object> field : fields.entrySet()) {
			String fName = field.getKey();
			Object fValue = field.getValue();
			document.addField(fName, fValue);
		}
		return document;
	}
	
	protected MarcMappingScript getMappingScript(HarvestedRecord record) {
		return mappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mappingScript = marcScriptFactory.create(getClass().getResourceAsStream("/marc/groovy/Base.groovy"));
	}

}
