package cz.mzk.recordmanager.server.index;

import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

// FIXME: todo mapping to solr fields
@Component
public class DublinCoreSolrRecordMapper implements SolrRecordMapper {

	private final static String FORMAT = "dublinCore";

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		SolrInputDocument document = new SolrInputDocument();
		return document;
	}

	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrInputDocument document = new SolrInputDocument();
		return document;
	}

}
