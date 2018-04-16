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

	protected Map<String, Object> parseAsDedupRecord(HarvestedRecord hrRecord) {
		InputStream is = new ByteArrayInputStream(hrRecord.getRawRecord());
		MarcRecord marcRecord = marcXmlParser.parseRecord(is);
		MappingScript<MarcFunctionContext> script = getDedupMappingScript(hrRecord);
		MarcFunctionContext ctx = new MarcFunctionContext(marcRecord, hrRecord, metadataRecordFactory.getMetadataRecord(hrRecord, marcRecord));
		return script.parse(ctx);
	}

	protected Map<String, Object> parseAsLocalRecord(HarvestedRecord hrRecord) {
		InputStream is = new ByteArrayInputStream(hrRecord.getRawRecord());
		MarcRecord marcRecord = marcXmlParser.parseRecord(is);
		MarcFunctionContext ctx = new MarcFunctionContext(marcRecord, hrRecord, metadataRecordFactory.getMetadataRecord(hrRecord, marcRecord));
		return getHarvestedMappingScript(hrRecord).parse(ctx);
	}

	protected MappingScript<MarcFunctionContext> getDedupMappingScript(HarvestedRecord hrRecord) {
		MappingScript<MarcFunctionContext> script = dedupRecordMappingScripts.get(hrRecord.getHarvestedFrom().getId());
		if (script == null) {
			script = getScript(hrRecord.getHarvestedFrom().getMappingDedupScript(), defaultDedupRecordMappingScript);
			dedupRecordMappingScripts.put(hrRecord.getHarvestedFrom().getId(), script);
		}
		return script;
	}

	protected MappingScript<MarcFunctionContext> getHarvestedMappingScript(HarvestedRecord hrRecord) {
		MappingScript<MarcFunctionContext> script = harvestedRecordMappingScripts.get(hrRecord.getHarvestedFrom().getId());
		if (script == null) {
			script = getScript(hrRecord.getHarvestedFrom().getMappingScript(), defaultHarvestedRecordMappingScript);
			harvestedRecordMappingScripts.put(hrRecord.getHarvestedFrom().getId(), script);
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
