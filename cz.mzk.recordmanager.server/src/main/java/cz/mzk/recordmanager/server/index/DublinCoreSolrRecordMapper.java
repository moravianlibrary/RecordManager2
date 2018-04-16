package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.ResourceProvider;
import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreFunctionContext;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreScriptFactory;

@Component
public class DublinCoreSolrRecordMapper implements SolrRecordMapper,
		InitializingBean {

	private final static String FORMAT = "dublinCore";
	private final static String FORMAT_ESE = "ese";

	@Autowired
	private DublinCoreScriptFactory dublinCoreScriptFactory;

	@Autowired
	private DublinCoreParser parser;
	
	@Autowired
	private ResourceProvider resourceProvider;

	@Autowired
	private MetadataRecordFactory metadataRecordFactory;
	
	private MappingScript<DublinCoreFunctionContext> dedupRecordMappingScript;
	
	private MappingScript<DublinCoreFunctionContext> harvestedRecordMappingScript;
	
	@Override
	public List<String> getSupportedFormats() {
		return Arrays.asList(FORMAT, FORMAT_ESE);
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
	public Map<String, Object> map(HarvestedRecord hrRecord) {
		return parseAsLocalRecord(hrRecord);
	}

	protected Map<String, Object> parseAsDedupRecord(HarvestedRecord hrRecord) {
		InputStream is = new ByteArrayInputStream(hrRecord.getRawRecord());
		MappingScript<DublinCoreFunctionContext> script = getMappingScript(hrRecord);
		DublinCoreRecord dcRecord = parser.parseRecord(is);
		DublinCoreFunctionContext dcContext = new DublinCoreFunctionContext(dcRecord, hrRecord, metadataRecordFactory.getMetadataRecord(hrRecord, dcRecord));
		return script.parse(dcContext);
	}
	
	protected Map<String, Object> parseAsLocalRecord(HarvestedRecord hrRecord){
		InputStream is = new ByteArrayInputStream(hrRecord.getRawRecord());
		DublinCoreRecord dcRecord = parser.parseRecord(is);
		DublinCoreFunctionContext dcContext = new DublinCoreFunctionContext(dcRecord, hrRecord, metadataRecordFactory.getMetadataRecord(hrRecord, dcRecord));
		return harvestedRecordMappingScript.parse(dcContext);
	}

	protected MappingScript<DublinCoreFunctionContext> getMappingScript(
			HarvestedRecord record) {
		return dedupRecordMappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dedupRecordMappingScript = dublinCoreScriptFactory.create( //
				resourceProvider.getResource("/dc/groovy/BaseDC.groovy"));
		harvestedRecordMappingScript = dublinCoreScriptFactory.create( //
				resourceProvider.getResource("/dc/groovy/HarvestedRecordBaseDC.groovy"));
	}

}
