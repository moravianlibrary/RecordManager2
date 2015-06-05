package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.marc.MarcMappingScript;
import cz.mzk.recordmanager.server.scripting.marc.MarcScriptFactory;

@Component
public class MarcSolrRecordMapper implements SolrRecordMapper, InitializingBean {

	private final static String FORMAT = "marc21-xml";

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private MarcScriptFactory marcScriptFactory;

	private MarcMappingScript mappingScript;

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public Map<String, Object> map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		HarvestedRecord record = records.get(0);
		return parse(record);
	}

	@Override
	public Map<String, Object> map(HarvestedRecord record) {
		return parse(record);
	}

	protected Map<String, Object> parse(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		MarcRecord rec = marcXmlParser.parseRecord(is);
		MarcMappingScript script = getMappingScript(record);
		return script.parse(rec);
	}

	protected MarcMappingScript getMappingScript(HarvestedRecord record) {
		return mappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mappingScript = marcScriptFactory.create(getClass()
				.getResourceAsStream("/marc/groovy/BaseMarc.groovy"));
	}

}
