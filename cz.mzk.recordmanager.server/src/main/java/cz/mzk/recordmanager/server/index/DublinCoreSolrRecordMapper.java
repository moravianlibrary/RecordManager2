package cz.mzk.recordmanager.server.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreScriptFactory;

@Component
public class DublinCoreSolrRecordMapper implements SolrRecordMapper, InitializingBean {

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
		// nacist fulltextmonography, dostat string
		System.out.println("*** jsem na miste kde se nacitaji fulltexty ***");
		String fulltext = getFulltextsFromRecord(record);
		MappingScript<DublinCoreRecord> script = getMappingScript(record);
		DublinCoreRecord rec = parser.parseRecord(is);
		Map<String, Object> fields = script.parse(rec);
		// pridat fulltextMonographyFullString do fields
		fields.put("fulltext", fulltext);
		return fields;
	}

	protected MappingScript<DublinCoreRecord> getMappingScript(HarvestedRecord record) {
		return mappingScript;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mappingScript = dublinCoreScriptFactory.create(getClass()
				.getResourceAsStream("/dc/groovy/BaseDC.groovy"));
	}

	public String getFulltextsFromRecord (HarvestedRecord record)  {
		List<FulltextMonography> pages = record.getFulltextMonography();
		String text ="";
		int i = 0;
	
	//	System.out.println("--- zacinam nacitat Fulltext ---");
	
		for (FulltextMonography page : pages) {
			i++;
	//		System.out.println("--- nacitam stranku cislo "+i+" ---");
			String uuid = page.getUuidPage();
			byte[] bytes = page.getFulltext();
			
	//		System.out.println("---- stranka ma uuid: "+uuid);
			try {
				if (bytes!=null) {
					System.out.println("---- stranka ma text ----");
	//				System.out.println(new String(bytes,"UTF-8"));
					text = text + new String(bytes,"UTF-8");
				} else {
					System.out.println("---- stranka nema text ----");	
				}
			} catch (UnsupportedEncodingException e) {
				System.out.println("--- chycena vyjimka UsnupportedEncodingException ---");
			}
			
		}
		
	//	System.out.println("--- Kompletní text ----");
	//	System.out.println(text);

		
		return text;
	}
	
}
