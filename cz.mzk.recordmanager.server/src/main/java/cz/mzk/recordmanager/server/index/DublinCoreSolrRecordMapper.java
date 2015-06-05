package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.SolrInputDocument;
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
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		HarvestedRecord record = records.get(0);
		SolrInputDocument document = parse(record);
		return document;
	}

	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrInputDocument document = parse(record);
		return document;
	}

	protected SolrInputDocument parse(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		SolrInputDocument document = new SolrInputDocument();
		DublinCoreMappingScript script = getMappingScript(record);
		DublinCoreRecord rec = parser.parseRecord(is);
		Map<String, Object> fields = script.parse(rec);
		for (Entry<String, Object> field : fields.entrySet()) {
			String fName = field.getKey();
			Object fValue = field.getValue();
			document.addField(fName, fValue);
		}
		return document;
	}

	protected DublinCoreMappingScript getMappingScript(HarvestedRecord record) {
		return mappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mappingScript = dublinCoreScriptFactory.create(getClass()
				.getResourceAsStream("/marc/groovy/BaseMarc.groovy"));
	}

}
