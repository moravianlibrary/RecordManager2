package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.SiglaAllDAO;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZiskejEddHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private SiglaAllDAO siglaAllDAO;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		if (!document.containsKey(SolrFieldConstants.ZISKEJ_EDD_WITHOUT_API_BOOLEAN)) return;
		boolean ziskej = (boolean) document.getFieldValue(SolrFieldConstants.ZISKEJ_EDD_WITHOUT_API_BOOLEAN);
		if (!ziskej) return;
		document.addField(SolrFieldConstants.ZISKEJ_EDD_SIGLAS, siglaAllDAO.findZiskejEddSigla(record.getHarvestedFrom()));
	}
}
