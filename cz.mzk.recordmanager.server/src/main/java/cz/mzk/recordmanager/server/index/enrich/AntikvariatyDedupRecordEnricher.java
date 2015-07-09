package cz.mzk.recordmanager.server.index.enrich;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.oai.dao.AntikvariatyRecordDAO;

@Component
public class AntikvariatyDedupRecordEnricher implements DedupRecordEnricher {

	@Autowired
	private AntikvariatyRecordDAO antikvariatyRecordDao;

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument) {
		String antikvariatyURL = antikvariatyRecordDao.getLinkToAntikvariaty(record);
		if (antikvariatyURL != null) {
			mergedDocument.addField(SolrFieldConstants.EXTERNAL_LINKS_FIELD, antikvariatyURL);
		}
	}

}
