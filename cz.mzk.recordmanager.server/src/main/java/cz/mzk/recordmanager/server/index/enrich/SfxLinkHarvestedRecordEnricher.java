package cz.mzk.recordmanager.server.index.enrich;

import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class SfxLinkHarvestedRecordEnricher implements HarvestedRecordEnricher {

	/**
	 * add institution prefix to sfx links
	 * @param record
	 * @param document
	 */
	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		String institutionCode = record.getHarvestedFrom().getIdPrefix();
		Set<String> links = new HashSet<>();
		if (document.containsKey(SolrFieldConstants.SFX_LINKS_FIELD)) {
			for (Object obj: document.getFieldValues(SolrFieldConstants.SFX_LINKS_FIELD)) {
				if (obj instanceof String) {
					links.add(institutionCode + "|" + (String) obj);
				}
			}
		}
		document.setField(SolrFieldConstants.SFX_LINKS_FIELD, links);
	}

}
