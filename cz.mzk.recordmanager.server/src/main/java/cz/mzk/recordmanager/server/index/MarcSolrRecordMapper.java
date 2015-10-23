package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.scripting.marc.MarcScriptFactory;

@Component
public class MarcSolrRecordMapper implements SolrRecordMapper, InitializingBean {

	private final static String FORMAT = "marc21-xml";

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private MarcScriptFactory marcScriptFactory;

	@Autowired
	private ResourceProvider resourceProvider;

	private MappingScript<MarcFunctionContext> dedupRecordMappingScript;

	private MappingScript<MarcFunctionContext> harvestedRecordMappingScript;

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
		return parseAsDedupRecord(record);
	}

	@Override
	public Map<String, Object> map(HarvestedRecord record) {
		return parseAsLocalRecord(record);
	}

	protected Map<String, Object> parseAsDedupRecord(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		MarcRecord rec = marcXmlParser.parseRecord(is);
		MappingScript<MarcFunctionContext> script = getMappingScript(record);
		MarcFunctionContext ctx = new MarcFunctionContext(rec);
		Map<String, Object> result = script.parse(ctx);
		return result;
	}

	protected Map<String, Object> parseAsLocalRecord(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		MarcRecord rec = marcXmlParser.parseRecord(is);
		MarcFunctionContext ctx = new MarcFunctionContext(rec);
		return harvestedRecordMappingScript.parse(ctx);
	}

	protected MappingScript<MarcFunctionContext> getMappingScript(HarvestedRecord record) {
		return dedupRecordMappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dedupRecordMappingScript = marcScriptFactory.create( //
				resourceProvider.getResource("/marc/groovy/BaseMarc.groovy"));
		harvestedRecordMappingScript = marcScriptFactory.create( //
				resourceProvider.getResource("/marc/groovy/HarvestedRecordBaseMarc.groovy"));
	}

}
