package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

@Component
public class SimpleAvailabilityFacetHarvestedRecordEnricher implements HarvestedRecordEnricher {

	private static final Pattern URL_ONLINE = Pattern.compile("^[^|]*\\|" + Constants.DOCUMENT_AVAILABILITY_ONLINE + "\\|.*");
	private static final Pattern URL_PROTECTED = Pattern.compile("^[^|]*\\|" + Constants.DOCUMENT_AVAILABILITY_PROTECTED + "\\|.*");

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		Collection<Object> statuses = document.getFieldValues(SolrFieldConstants.STATUSES_FACET);
		if (statuses == null) statuses = new ArrayList<>();
		// contains public URL?
		Collection<Object> urls = document.getFieldValues(SolrFieldConstants.URL);
		if (urls == null) {
			return;
		}
		for (Object url : urls) {
			if (URL_ONLINE.matcher((String) url).matches()) statuses.add(Constants.DOCUMENT_AVAILABILITY_ONLINE);
			if (URL_PROTECTED.matcher((String) url).matches()) statuses.add(Constants.DOCUMENT_AVAILABILITY_PROTECTED);
		}
		if (!statuses.isEmpty()) {
			document.setField(SolrFieldConstants.STATUSES_FACET, statuses);
		}
	}
}
