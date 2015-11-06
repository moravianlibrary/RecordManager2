package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.DelegatingSolrRecordMapper;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.FulltextMonographyDAO;

@Component
public class FullTextEnricher implements DedupRecordEnricher {

	private static Logger logger = LoggerFactory.getLogger(DelegatingSolrRecordMapper.class);

	// line ending with '-', following line starts with letter
	// '¬' is sometimes seen in Kramerius OCR on place of hyphen
	protected static final Pattern TEXT_HYPHENATED_WORDS = Pattern.compile("[-,¬]\\s*\\n(\\p{L})");

	// newline without hyphen
	protected static final Pattern TEXT_NEWLINES = Pattern.compile("\\s*\\n\\s*");

	// tabelators
	protected static final Pattern TEXT_TAB = Pattern.compile("\\s*\\t\\s*");

	@Autowired
	private FulltextMonographyDAO monographyDao;

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		List<String> text = monographyDao.getFullText(record);
		if (text.isEmpty()) {
			return;
		}
		StringBuilder txt = new StringBuilder();
		text.stream().forEach(it -> txt.append(modifyFulltextPage(it)));
		logger.info("Enriching record {} with fulltext: {}", record, txt.toString());
		mergedDocument.setField("fulltext", txt.toString());
	}

	private String modifyFulltextPage(String fulltextPage) {
		fulltextPage = TEXT_HYPHENATED_WORDS.matcher(fulltextPage).replaceAll("$1");
		fulltextPage = TEXT_NEWLINES.matcher(fulltextPage).replaceAll(" ");
		fulltextPage = TEXT_TAB.matcher(fulltextPage).replaceAll(" ");
		return fulltextPage;
	}

}
