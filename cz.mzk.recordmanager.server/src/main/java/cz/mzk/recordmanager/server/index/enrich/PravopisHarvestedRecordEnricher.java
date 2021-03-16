package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Pravopis;
import cz.mzk.recordmanager.server.oai.dao.hibernate.PravopisDAOHibernate;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PravopisHarvestedRecordEnricher implements HarvestedRecordEnricher {

	@Autowired
	private PravopisDAOHibernate pravopisDAOHibernate;

	@Override
	public void enrich(HarvestedRecord record, SolrInputDocument document) {
		List<String> results = new ArrayList<>();
		if (document.containsKey(SolrFieldConstants.TITLE_SEARCH_TXT_MV)) {
			for (Object title : document.getFieldValues(SolrFieldConstants.TITLE_SEARCH_TXT_MV)) {
				for (String word : title.toString().split("\\b")) {
					Pravopis pravopis = pravopisDAOHibernate.findByKey(word);
					List<String> temp = new ArrayList<>();
					for (String alterTitle : results) {
						temp.add(alterTitle + word);
					}
					if (results.isEmpty()) temp.add(word);
					if (pravopis != null) {
						String alterWord = pravopis.getValue();
						for (String alterTitle : results) {
							temp.add(alterTitle + alterWord);
						}
						if (results.isEmpty()) temp.add(alterWord);
					}
					results = temp;
				}
				results.remove(title);
			}
			document.setField(SolrFieldConstants.PRAVOPIS_TXT_MV, results);
		}
	}

}
