package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.kramerius.fulltext.KrameriusFulltexter;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreScriptFactory;

@Component
public class DublinCoreSolrRecordMapper implements SolrRecordMapper,
		InitializingBean {

	private static Logger logger = LoggerFactory
			.getLogger(DublinCoreSolrRecordMapper.class);
	
	private final static String FORMAT = "dublinCore";

	@Autowired
	private DublinCoreScriptFactory dublinCoreScriptFactory;

	@Autowired
	private DublinCoreParser parser;

	private MappingScript<DublinCoreRecord> mappingScript;

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

		// loading fulltext
		String fulltext = getFulltextsFromRecord(record);
		MappingScript<DublinCoreRecord> script = getMappingScript(record);
		DublinCoreRecord rec = parser.parseRecord(is);
		Map<String, Object> fields = script.parse(rec);
		// adding fulltext to SOLR fields..
		fields.put("fulltext", fulltext);
		return fields;
	}

	protected MappingScript<DublinCoreRecord> getMappingScript(
			HarvestedRecord record) {
		return mappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mappingScript = dublinCoreScriptFactory.create(getClass()
				.getResourceAsStream("/dc/groovy/BaseDC.groovy"));
	}

	public String getFulltextsFromRecord(HarvestedRecord record) {
		List<FulltextMonography> pages = record.getFulltextMonography();
		String text = "";
		int i = 0;

		for (FulltextMonography page : pages) {
			i++;
			String uuid = page.getUuidPage();
			byte[] bytes = page.getFulltext();

			try {
				if (bytes != null) {
					logger.debug("Page with uuid ["+ uuid +"] has text and was added to indexed fulltext String");
					text = text + new String(bytes, "UTF-8");
				} else {
					logger.debug("Page with uuid ["+ uuid +"] has NO text and was NOT added to indexed fulltext String");
				}
			} catch (UnsupportedEncodingException e) {
				logger.warn("UsnupportedEncodingException: "+ e.getMessage());
			}

		}

		// logger.debug("Complete text:");
		// logger.debug(text);

		return text;
	}

}
