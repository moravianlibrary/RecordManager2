package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

@Component
public class UrlDedupRecordEnricher implements DedupRecordEnricher {

	private static final String ONLINE = "online";
	private static final String UNKNOWN = "unknown";
	private static final String PROTECTED = "protected";
	private static final String SPLITTER = "\\|";
	private static final String JOINER = "|";
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<Object> urls = new HashSet<>();
		localRecords.stream()
			.filter(rec -> rec.getFieldValue(SolrFieldConstants.URL) != null)
			.forEach(rec -> urls.addAll(rec.getFieldValues(SolrFieldConstants.URL)));

		mergedDocument.remove(SolrFieldConstants.URL);
		mergedDocument.addField(SolrFieldConstants.URL, urlsFilter(urls));
		
		mergedDocument.remove(SolrFieldConstants.KRAMERIUS_DUMMY_RIGTHS);
		
		localRecords.stream().forEach(doc -> doc.remove(SolrFieldConstants.URL));
	}

	private List<String> urlsFilter(Set<Object> values){
		List<String> results = new ArrayList<>();
		
		Map<String, List<String>> urlsMap = new HashMap<String, List<String>>();
		
		for(Object obj: values){
			if(obj.toString().split(SPLITTER).length < 3){
				results.add(obj.toString());
			}
			else{
				String spliturl[] = obj.toString().split(SPLITTER);
				if(urlsMap.containsKey(spliturl[2])){
					List<String> urls = urlsMap.get(spliturl[2]);
//					online urls at the beginning
					if(spliturl[1].equals(ONLINE)) urls.add(0, obj.toString());
					else urls.add(obj.toString());
					urlsMap.put(spliturl[2], urls);
				}
				else{
					List<String> list = new ArrayList<>();
					list.add(obj.toString());
					urlsMap.put(spliturl[2], list);
				}
			}
		}

		for(String url: urlsMap.keySet()){
			Set<String> urls = new HashSet<>(urlsMap.get(url));
			boolean online = false;
			List<String> unknownlist = new ArrayList<>();
			for(String value: urls){
				String spliturl[] = value.split(SPLITTER);
				if(spliturl[1].equals(ONLINE)){
					results.add(value);
					online = true;
				}
				else{
					if(online) break;
					if(spliturl[1].equals(PROTECTED)) results.add(value);
					if(spliturl[1].equals(UNKNOWN)) unknownlist.add(value);
				}
			}
			
			if(unknownlist.size() == 1) results.addAll(unknownlist);
			if(unknownlist.size() > 1){
				String spliturl[] = unknownlist.get(0).split(SPLITTER);
				spliturl[0] = UNKNOWN;
				StringBuilder sb = new StringBuilder();
				sb.append(String.join(JOINER, spliturl));
				if(spliturl.length == 3) sb.append(JOINER);
				results.add(sb.toString());
			}
		}

		return results;
	}
	
}
