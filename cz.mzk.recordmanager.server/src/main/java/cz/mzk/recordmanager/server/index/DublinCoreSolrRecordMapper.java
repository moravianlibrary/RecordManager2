package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreMappingScript;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreScriptFactory;

@Component
public class DublinCoreSolrRecordMapper implements SolrRecordMapper, InitializingBean {

	private final static String FORMAT = "dublinCore";

	@Autowired
	private DublinCoreScriptFactory dublinCoreScriptFactory;

	@Autowired
	private DublinCoreParser parser;

	private DublinCoreMappingScript mappingScript;

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
		DublinCoreMappingScript script = getMappingScript(record);
		DublinCoreRecord rec = parser.parseRecord(is);
		Map<String, Object> fields = script.parse(rec);
		return fields;
	}

	protected DublinCoreMappingScript getMappingScript(HarvestedRecord record) {
		return mappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mappingScript = dublinCoreScriptFactory.create(getClass()
				.getResourceAsStream("/dc/groovy/BaseDC.groovy"));
	}

}
