package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AuthorityRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.AuthorityRecordDAO;

@Component
public class AuthorityEnricher implements DedupRecordEnricher {

	@Autowired
	private AuthorityRecordDAO authorityRecordDao;
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		for (AuthorityRecord auth: authorityRecordDao.findByDedupRecord(record)) {
			// TODO enriching stuff
		}
		
	}

}
