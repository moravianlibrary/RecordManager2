package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

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

	@Autowired
	private FulltextMonographyDAO monographyDao;

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		List<String> text = monographyDao.getFullText(record);
		if (text.isEmpty()) {
			return;
		}
		logger.info("Enriching record {} with fulltext", record);
		StringBuilder txt = new StringBuilder();
		text.stream().forEach(it -> txt.append(it));
		mergedDocument.setField("fulltext", txt.toString());
	}

}
