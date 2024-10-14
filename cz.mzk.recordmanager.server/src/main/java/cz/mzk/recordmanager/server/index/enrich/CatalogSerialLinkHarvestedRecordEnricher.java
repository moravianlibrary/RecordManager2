package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.SiglaAll;
import cz.mzk.recordmanager.server.util.CaslinLink;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CatalogSerialLinkHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private CaslinLink caslinLink;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		if (!record.getHarvestedFrom().isCatalogSerialLink()) return;
		if (!document.containsKey(SolrFieldConstants.MAPPINGS996)
				|| document.getFieldValues(SolrFieldConstants.MAPPINGS996) == null
				|| !document.getFieldValues(SolrFieldConstants.MAPPINGS996).isEmpty()) return;
		Object recordFormat = document.getFieldValue(SolrFieldConstants.RECORD_FORMAT_DISPLAY);
		if (recordFormat != null && !recordFormat.toString().contains(HarvestedRecordFormatEnum.PERIODICALS.name()))
			return;
		if (!document.containsKey(SolrFieldConstants.ID001_STR)) return;
		String id = document.getFieldValue(SolrFieldConstants.ID001_STR).toString();
		List<SiglaAll> siglas = record.getHarvestedFrom().getSiglaAlls();
		if (siglas == null || siglas.isEmpty()) return;
		String sigla = siglas.get(0).getSigla();

		String link = caslinLink.getCaslinLink(sigla, id);
		if (link == null || link.isEmpty()) return;

		List<String> results = new ArrayList<>();
		results.add(MetadataUtils.generateUrl(record.getHarvestedFrom().getIdPrefix(),
				Constants.DOCUMENT_AVAILABILITY_UNKNOWN, link, EVersionConstants.CATALOG_SERIAL_LINK));
		document.addField(SolrFieldConstants.CATALOG_SERIAL_LINK, results);
	}

}
