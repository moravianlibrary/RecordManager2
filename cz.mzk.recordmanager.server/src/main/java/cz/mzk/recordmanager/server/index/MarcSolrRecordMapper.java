package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
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
	
	@Autowired
	private MetadataRecordFactory metadataRecordFactory;

	private Map<Long, MappingScript<MarcFunctionContext>> dedupRecordMappingScripts = new ConcurrentHashMap<Long, MappingScript<MarcFunctionContext>>(10, 0.75f, 1);

	private Map<Long, MappingScript<MarcFunctionContext>> harvestedRecordMappingScripts = new ConcurrentHashMap<Long, MappingScript<MarcFunctionContext>>(10, 0.75f, 1);

	private MappingScript<MarcFunctionContext> defaultHarvestedRecordMappingScript;

	private MappingScript<MarcFunctionContext> defaultDedupRecordMappingScript;

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
		MappingScript<MarcFunctionContext> script = getDedupMappingScript(record);
		MarcFunctionContext ctx = new MarcFunctionContext(rec, record, metadataRecordFactory.getMetadataRecord(record));
		Map<String, Object> result = script.parse(ctx);
		return result;
	}

	protected Map<String, Object> parseAsLocalRecord(HarvestedRecord record) {
		InputStream is = new ByteArrayInputStream(record.getRawRecord());
		MarcRecord rec = marcXmlParser.parseRecord(is);
		MarcFunctionContext ctx = new MarcFunctionContext(rec, record, metadataRecordFactory.getMetadataRecord(record));
		return getHarvestedMappingScript(record).parse(ctx);
	}

	protected MappingScript<MarcFunctionContext> getDedupMappingScript(HarvestedRecord record) {
		MappingScript<MarcFunctionContext> script = dedupRecordMappingScripts.get(record.getHarvestedFrom().getId());
		if (script == null) {
			script = getScript(record.getHarvestedFrom().getMappingDedupScript(), defaultDedupRecordMappingScript);
			dedupRecordMappingScripts.put(record.getHarvestedFrom().getId(), script);
		}
		return script;
	}

	protected MappingScript<MarcFunctionContext> getHarvestedMappingScript(HarvestedRecord record) {
		MappingScript<MarcFunctionContext> script = harvestedRecordMappingScripts.get(record.getHarvestedFrom().getId());
		if (script == null) {
			script = getScript(record.getHarvestedFrom().getMappingScript(), defaultHarvestedRecordMappingScript);
			harvestedRecordMappingScripts.put(record.getHarvestedFrom().getId(), script);
		}
		return script;
	}

	protected MappingScript<MarcFunctionContext> getScript(String mappingScript, MappingScript<MarcFunctionContext> defaultScript) {
		if (mappingScript != null) {
			String[] scripts = mappingScript.split(",");
			InputStream resources[] = new InputStream[scripts.length];
			int index = 0;
			for (String script : scripts) {
				try {
					resources[index] = resourceProvider.getResource("/marc/groovy/" + script.trim());
					index++;
				} catch (IOException ioe) {
					throw new RuntimeException(ioe.getMessage(), ioe);
				}
			}
			return marcScriptFactory.create(resources);
		} else {
			return defaultScript;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		defaultDedupRecordMappingScript = marcScriptFactory.create( //
				resourceProvider.getResource("/marc/groovy/BaseMarc.groovy"));
		defaultHarvestedRecordMappingScript = marcScriptFactory.create( //
				resourceProvider.getResource("/marc/groovy/HarvestedRecordBaseMarc.groovy"));
	}

}
