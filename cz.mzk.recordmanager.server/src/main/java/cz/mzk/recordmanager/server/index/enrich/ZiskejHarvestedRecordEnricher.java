package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ZiskejHarvestedRecordEnricher implements
		HarvestedRecordEnricher {

	private static final List<String> FORMAT_ALLOWED = new ArrayList<>();

	static {
		FORMAT_ALLOWED.addAll(SolrUtils.createRecordTypeHierarchicFacet(HarvestedRecordFormatEnum.BOOKS));
	}

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		if (document.containsKey(SolrFieldConstants.ZISKEJ_BOOLEAN_HIDDEN)
				&& (Boolean) document.getFieldValue(SolrFieldConstants.ZISKEJ_BOOLEAN_HIDDEN)
				&& document.containsKey(SolrFieldConstants.RECORD_FORMAT_DISPLAY)
				&& !Collections.disjoint(document.getFieldValues(SolrFieldConstants.RECORD_FORMAT_DISPLAY), FORMAT_ALLOWED)) {
			document.addField(SolrFieldConstants.ZISKEJ_BOOLEAN, true);
		}
	}

}
