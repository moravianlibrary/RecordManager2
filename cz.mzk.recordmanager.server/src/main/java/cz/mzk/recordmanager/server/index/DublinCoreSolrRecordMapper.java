package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

	// line ending with '-', following line starts with letter
	// '¬' is sometimes seen in Kramerius OCR on place of hyphen
	protected static final Pattern TEXT_HYPHENATED_WORDS = Pattern.compile("[-,¬]\\s*\\n(\\p{L})");
	// newline without hyphen
	protected static final Pattern TEXT_NEWLINES = Pattern.compile("\\s*\\n\\s*");
	// tabelators
	protected static final Pattern TEXT_TAB = Pattern.compile("\\s*\\t\\s*");
	
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

		for (FulltextMonography page : pages) {
			String uuid = page.getUuidPage();
			byte[] bytes = page.getFulltext();

			try {
				if (bytes != null) {
					String fulltextPage = new String (bytes, "UTF-8");
					fulltextPage = modifyFulltextPage(fulltextPage);					
					text = text + fulltextPage;
					logger.debug("Page with uuid ["+ uuid +"] has text and was added to indexed fulltext String");

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
	
	public String modifyFulltextPage(String fulltextPage) {
		fulltextPage = TEXT_HYPHENATED_WORDS.matcher(fulltextPage).replaceAll("$1");
		fulltextPage = TEXT_NEWLINES.matcher(fulltextPage).replaceAll(" ");
		fulltextPage = TEXT_TAB.matcher(fulltextPage).replaceAll(" ");
				
		return fulltextPage;
	}

}
