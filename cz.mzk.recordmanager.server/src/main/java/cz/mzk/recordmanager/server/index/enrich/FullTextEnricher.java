package cz.mzk.recordmanager.server.index.enrich;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.DelegatingSolrRecordMapper;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
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
		//logger.info("Enriching record {} with fulltext: {}", record, txt.toString());
		logger.info("Enriching record {} with fulltext begining with: {}...", record,  StringUtils.substring(txt.toString(), 0, 30));
		
		try {
			final byte[] utf8Bytes = txt.toString().getBytes("UTF-8");
			logger.info("Total size of fulltext is :"+utf8Bytes.length);
		} catch (UnsupportedEncodingException e) {
			logger.info("Can't calculate size of fulltext - UnsupportedEncodingException caught.");
		}
		mergedDocument.setField(SolrFieldConstants.FULLTEXT_FIELD, txt.toString());
	}

	private String modifyFulltextPage(String fulltextPage) {
		fulltextPage = TEXT_HYPHENATED_WORDS.matcher(fulltextPage).replaceAll("$1");
		fulltextPage = TEXT_NEWLINES.matcher(fulltextPage).replaceAll(" ");
		fulltextPage = TEXT_TAB.matcher(fulltextPage).replaceAll(" ");
		return fulltextPage;
	}

}
