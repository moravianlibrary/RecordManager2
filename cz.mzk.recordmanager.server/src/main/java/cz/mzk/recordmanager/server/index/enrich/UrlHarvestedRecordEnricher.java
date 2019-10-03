package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class UrlHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private FulltextKrameriusDAO fulltextKrameriusDAO;

	private static final Pattern UNKNOWN = Pattern.compile("unknown");
	private static final Pattern OBALKA = Pattern.compile("\\|obálka", Pattern.CASE_INSENSITIVE);

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
		urls.forEach(url -> result.add(institutionCode + '|' + updateKrameriusPolicy(record, url)));
		document.addField(SolrFieldConstants.URL,
				result.stream().filter(url -> !OBALKA.matcher(url).find()).collect(Collectors.toSet()));
	}

	private String updateKrameriusPolicy(HarvestedRecord hr, String url) {
		if (hr.getHarvestedFrom().getIdPrefix() != null && hr.getHarvestedFrom().getIdPrefix().startsWith("kram-")
				&& url.startsWith("unknown")) {
			url = CleaningUtils.replaceFirst(url, UNKNOWN, fulltextKrameriusDAO.getPolicy(hr.getId()));
		}
		return url;
	}

}
