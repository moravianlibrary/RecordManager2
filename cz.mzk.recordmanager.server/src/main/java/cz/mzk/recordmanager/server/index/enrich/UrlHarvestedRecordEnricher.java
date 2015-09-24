package cz.mzk.recordmanager.server.index.enrich;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class UrlHarvestedRecordEnricher implements HarvestedRecordEnricher {

	private Map<Long,String> krameriusBaseLinkMap = new ConcurrentHashMap<>();

	@Autowired
	private KrameriusConfigurationDAO krameriusConfiguationDao;

	/**
	 * generate links in standard format "institution code"|"policy code"|"url"
	 * removes field KRAMERIUS_DUMMY_RIGTHS from document
	 * @param record
	 * @param document
	 * @return
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
		
		//handle Kramerius url
		String kramUrl = null;
		if (Constants.METADATA_FORMAT_DUBLIN_CORE.equals(record.getFormat())) {
			Long importConfId = record.getHarvestedFrom().getId();
			if (!krameriusBaseLinkMap.containsKey(importConfId)) {
				KrameriusConfiguration kramConf = krameriusConfiguationDao.get(importConfId);
				if (kramConf.getUrl() != null) {
					String kramUrlBase = Pattern.compile("api/v\\d\\.\\d").matcher(kramConf.getUrl()).replaceAll("");
					krameriusBaseLinkMap.put(importConfId, kramUrlBase);
				}
			}
	
			if (krameriusBaseLinkMap.containsKey(importConfId)) {
				String policy = (String) document.getFieldValue(SolrFieldConstants.KRAMERIUS_DUMMY_RIGTHS);
				// FIXME probably not best way of generating urls
				kramUrl = policy + "|" + krameriusBaseLinkMap.get(importConfId) + "i.jsp?pid=" + record.getUniqueId().getRecordId() + "|";
				urls.add(kramUrl);
			}
		}
		
		document.remove(SolrFieldConstants.KRAMERIUS_DUMMY_RIGTHS);
		document.remove(SolrFieldConstants.URL);
		
		Set<String> result = new HashSet<>();
		urls.stream().forEach(url -> result.add(institutionCode + "|" + url));
		document.addField(SolrFieldConstants.URL, result);
	}

}
