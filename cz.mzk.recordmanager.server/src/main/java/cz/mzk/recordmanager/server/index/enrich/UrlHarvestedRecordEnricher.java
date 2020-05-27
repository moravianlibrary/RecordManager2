package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.EVersionUrl;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.oai.dao.KramAvailabilityDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class UrlHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private FulltextKrameriusDAO fulltextKrameriusDAO;

	@Autowired
	private KramAvailabilityDAO kramAvailabilityDAO;

	private static final Pattern OBALKA = Pattern.compile("\\|obálka", Pattern.CASE_INSENSITIVE);
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
		Set<String> result = new HashSet<>();
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
			if (UrlValidator.getInstance().isValid(url.getLink())) result.add(url.toString());
		}
		document.remove(SolrFieldConstants.URL);
		document.addField(SolrFieldConstants.URL,
				result.stream().filter(url -> !OBALKA.matcher(url).find()).collect(Collectors.toSet()));
	}

}
