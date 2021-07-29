package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.UrlValidatorUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UrlHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private FulltextKrameriusDAO fulltextKrameriusDAO;

	@Autowired
	private KramAvailabilityDAO kramAvailabilityDAO;

	private static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");

	/**
	 * generate links in standard format "institution code"|"policy code"|"url"
	 * removes field KRAMERIUS_DUMMY_RIGTHS from document
	 *
	 * @param record   {@link HarvestedRecord}
	 * @param document {@link SolrInputDocument}
	 */
	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		Set<EVersionUrl> urls = new HashSet<>();
		if (document.containsKey(SolrFieldConstants.URL)) {
			for (Object obj: document.getFieldValues(SolrFieldConstants.URL)) {
				if (obj instanceof String) {
					try {
						urls.add(EVersionUrl.create(obj.toString()));
					} catch (Exception e) {
						continue;
					}
				}
			}
		}
		Set<String> results = new HashSet<>();
		for (EVersionUrl url : urls) {
			if (url.getSource().startsWith("kram-")) { // url from kramerius
				Matcher matcher = UUID_PATTERN.matcher(url.getLink());
				if (!matcher.find()) continue; // has uuid
				KramAvailability availability = kramAvailabilityDAO
						.getByConfigAndUuid(record.getHarvestedFrom(), matcher.group(0));
				if (availability != null) { // has availability in table
					url.setAvailability(availability.getAvailability());
				} else {
					if (!url.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_UNKNOWN)) {
						// availability is unknown - get availability from fulltext
						url.setAvailability(fulltextKrameriusDAO.getPolicy(record.getId()));
					}
					// else availability is not unknown - availability from metadataRecord
				}
			}
			if (UrlValidatorUtils.doubleSlashUrlValidator().isValid(url.getLink())) results.add(url.toString());
		}
		document.remove(SolrFieldConstants.URL);
		document.addField(SolrFieldConstants.URL, results);
	}

}
