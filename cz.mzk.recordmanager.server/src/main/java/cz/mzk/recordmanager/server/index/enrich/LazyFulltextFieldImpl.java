package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.solr.LazyFulltextField;

public class LazyFulltextFieldImpl implements LazyFulltextField {

	private static Logger logger = LoggerFactory.getLogger(LazyFulltextFieldImpl.class);

	// line ending with '-', following line starts with letter
	// '¬' is sometimes seen in Kramerius OCR on place of hyphen
	protected static final Pattern TEXT_HYPHENATED_WORDS = Pattern.compile("[-,¬]\\s*\\n(\\p{L})");

	// newline without hyphen
	protected static final Pattern TEXT_NEWLINES = Pattern.compile("\\s*\\n\\s*");

	// tabelators
	protected static final Pattern TEXT_TAB = Pattern.compile("\\s*\\t\\s*");

	private final FulltextKrameriusDAO fulltextKrameriusDao;

	private final DedupRecord record;

	public LazyFulltextFieldImpl(FulltextKrameriusDAO fulltextKrameriusDao,
			DedupRecord record) {
		super();
		this.fulltextKrameriusDao = fulltextKrameriusDao;
		this.record = record;
	}

	@Override
	public String getContent() {
		List<String> text = fulltextKrameriusDao.getFullText(record);
		if (text.isEmpty()) {
			return null;
		}
		int size = text.stream().mapToInt(it -> it.length()).sum();
		StringBuilder txt = new StringBuilder(size);
		text.stream().forEach(it -> txt.append(modifyFulltextPage(it)));
		String fulltext = txt.toString();
		logger.info("Enriching record {} with fulltext begining with: {}...", record, StringUtils.substring(fulltext, 0, 30));
		return fulltext;
	}

	private String modifyFulltextPage(String fulltextPage) {
		fulltextPage = TEXT_HYPHENATED_WORDS.matcher(fulltextPage).replaceAll("$1");
		fulltextPage = TEXT_NEWLINES.matcher(fulltextPage).replaceAll(" ");
		fulltextPage = TEXT_TAB.matcher(fulltextPage).replaceAll(" ");
		return fulltextPage;
	}

}
