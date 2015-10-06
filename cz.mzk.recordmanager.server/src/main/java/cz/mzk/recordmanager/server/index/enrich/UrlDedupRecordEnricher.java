package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	private List<String> urlsFilter(Set<Object> urls){
		Set<String> duplicitUrls = new HashSet<>();
		List<String> results = new ArrayList<>();
		
		Set<String> helper = new HashSet<>();
		// duplicit urls
		for(Object obj: urls){
		    String spliturl[] = obj.toString().split(SPLITTER);
		    if(!helper.add(spliturl[2])) duplicitUrls.add(spliturl[2].toString());
		}

		// nonduplicit urls to result
		for(Object obj: urls){
		    String spliturl[] = obj.toString().split(SPLITTER);
		    if(!duplicitUrls.contains(spliturl[2])) results.add(obj.toString());
		}
		
		for(String url: duplicitUrls){
			List<String> duplurls = new ArrayList<String>();
			Boolean online = false;
			int unknowncount = 0;
			Boolean protect = false;
			
			for(Object obj: urls){
			    String spliturl[] = obj.toString().split(SPLITTER);
			    if(url.matches(spliturl[2])){
			    	switch (spliturl[1]) {
					case ONLINE:
						online = true;
						break;
					case UNKNOWN:
						unknowncount++;
						break;
					case PROTECTED:
						protect = true;
						break;
					}
			    	duplurls.add(obj.toString());			    
			    }
			}
			
			for(String line: duplurls){
				String spliturl[] = line.split(SPLITTER);
				if(online){
					if(spliturl[1].equals(ONLINE)) results.add(line);
				}
				else{
					if(unknowncount == 1){
						if(spliturl[1].equals(UNKNOWN)) results.add(line);
						unknowncount = 0;
					}
					if(unknowncount > 1){
						if(spliturl[1].equals(UNKNOWN)){
							spliturl[0] = UNKNOWN;
							StringBuilder sb = new StringBuilder();
							sb.append(String.join("|", spliturl));
							if(spliturl.length == 3) sb.append("|");
							results.add(sb.toString());
							unknowncount = 0;
						}
					}
					if(protect){
						if(spliturl[1].equals(PROTECTED)) results.add(line);
					}
				}
			}
		}
		
		return results;
	}
	
}
