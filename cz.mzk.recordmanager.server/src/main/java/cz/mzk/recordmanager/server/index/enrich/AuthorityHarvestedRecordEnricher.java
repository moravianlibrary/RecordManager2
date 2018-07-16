package cz.mzk.recordmanager.server.index.enrich;

import com.google.common.cache.CacheBuilder;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Component
public class AuthorityHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private HarvestedRecordDAO hrDao;

	private ConcurrentMap<String, String> cache = CacheBuilder.newBuilder()
			.maximumSize(50000L)
			.<String, String>build().asMap();

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		if (document.containsKey(SolrFieldConstants.AUTHOR_AUTHORITY_DISPLAY)) {
			String authority_id = (String) document.getFieldValue(SolrFieldConstants.AUTHOR_AUTHORITY_DISPLAY);
			String record_id = getValue(authority_id);
			if (record_id != null) document.addField(SolrFieldConstants.AUTHOR_AUTHORITY_ID_DISPLAY, record_id);
		}
		if (document.containsKey(SolrFieldConstants.AUTHORITY2_DISPLAY_MV)) {
			List<String> results = new ArrayList<>();
			for (Object object : document.getFieldValues(SolrFieldConstants.AUTHORITY2_DISPLAY_MV)) {
				String authority_id = (String) object;
				if (authority_id.isEmpty()) {
					results.add(authority_id);
					continue;
				}
				String record_id = getValue(authority_id);
				results.add(record_id == null ? "" : record_id);
			}
			document.addField(SolrFieldConstants.AUTHOR2_AUTHORITY_ID_DISPLAY_MV, results);
		}
		if (document.containsKey(SolrFieldConstants.PSEUDONYM_IDS_DISPLAY_MV)) {
			List<String> results = new ArrayList<>();
			for (Object object : document.getFieldValues(SolrFieldConstants.PSEUDONYM_IDS_DISPLAY_MV)) {
				String authority_id = (String) object;
				if (authority_id.isEmpty()) {
					results.add(authority_id);
					continue;
				}
				String record_id = getValue(authority_id);
				results.add(record_id == null ? "" : record_id);
			}
			document.addField(SolrFieldConstants.PSEUDONYM_RECORD_IDS_DISPLAY_MV, results);
		}
	}

	private String getValue(String authority_id) {
		if (cache.containsKey(authority_id)) return cache.get(authority_id);
		String record_id = hrDao.getRecordIdBy001(Constants.IMPORT_CONF_ID_AUTHORITY, authority_id);
		if (record_id != null) {
			cache.put(authority_id, "auth." + record_id);
			return "auth." + record_id;
		}
		return null;
	}

}
