package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;

@Component
public class FullTextEnricher implements DedupRecordEnricher {

	private static Logger logger = LoggerFactory.getLogger(FullTextEnricher.class);

	private long maxFullTextSize = 20_000_000L; // 20 MB

	private long directFullTextSize = 1_000_000L; // 1 MB

	@Autowired
	private FulltextKrameriusDAO fulltextKrameriusDAO;

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		long size = fulltextKrameriusDAO.getFullTextSize(record);
		if (size == 0) {
			return;
		}
		if (size >= maxFullTextSize) {
			logger.warn("Fulltext size {} B for dedup record {} is bigger than limit {} B for fulltext indexing",
					new Object[]{ size, record, maxFullTextSize});
			return;
		}
		LazyFulltextFieldImpl fetcher = new LazyFulltextFieldImpl(fulltextKrameriusDAO, record);
		if (size <= directFullTextSize) {
			mergedDocument.setField(SolrFieldConstants.FULLTEXT_FIELD, fetcher.getContent());
		} else {
			mergedDocument.setField(SolrFieldConstants.FULLTEXT_FIELD, fetcher);
		}
	}

}
