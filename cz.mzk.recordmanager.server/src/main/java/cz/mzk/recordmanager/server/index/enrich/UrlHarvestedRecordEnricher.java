package cz.mzk.recordmanager.server.index.enrich;

import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class UrlHarvestedRecordEnricher implements HarvestedRecordEnricher {

	/**
	 * generate links in standard format "institution code"|"policy code"|"url"
	 * removes field KRAMERIUS_DUMMY_RIGTHS from document
	 *
	 * @param record   {@link HarvestedRecord}
	 * @param document {@link SolrInputDocument}
	 */
	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		String institutionCode = record.getHarvestedFrom().getIdPrefix();
		Set<String> urls = new HashSet<>();
		if (document.containsKey(SolrFieldConstants.URL)) {
			for (Object obj: document.getFieldValues(SolrFieldConstants.URL)) {
				if (obj instanceof String) {
					urls.add((String)obj);
				}
			}
		}

		document.remove(SolrFieldConstants.URL);

		Set<String> result = new HashSet<>();
		urls.stream().forEach(url -> result.add(institutionCode + "|" + url));
		document.addField(SolrFieldConstants.URL, result);
	}

}
