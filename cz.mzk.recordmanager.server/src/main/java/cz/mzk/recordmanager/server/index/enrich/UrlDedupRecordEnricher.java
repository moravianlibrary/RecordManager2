package cz.mzk.recordmanager.server.index.enrich;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String KRAMERIUS_URL = "http://kramerius";
	private static final String KRAMERIUS_HANDLE = "handle/";
	private static final String KRAMERIUS_IJSP = "i.jsp?pid=";
	private static final Pattern KRAM_MZK_PATTERN = Pattern.compile("http://kramerius.mzk.cz.*(uuid:.*)");
	private static final String DIGITALNIKNIHOVNA = "http://www.digitalniknihovna.cz/mzk/uuid/";
	
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Set<Object> urls = new HashSet<>();
		localRecords.stream()
			.filter(rec -> rec.getFieldValue(SolrFieldConstants.URL) != null)
			.forEach(rec -> urls.addAll(rec.getFieldValues(SolrFieldConstants.URL)));

		mergedDocument.remove(SolrFieldConstants.URL);
		mergedDocument.addField(SolrFieldConstants.URL, urlsFilter(urls));
		
		localRecords.stream().forEach(doc -> doc.remove(SolrFieldConstants.URL));
	}

	/**
	 *
	 * @param values urls format "institution code"|"policy code"|"url"
	 * @return List of unique urls
	 */
	private List<String> urlsFilter(Set<Object> values){
		List<String> results = new ArrayList<>();
		
		Map<String, List<String>> urlsMap = new HashMap<String, List<String>>();
		
		for(Object obj: values){
			if(obj.toString().split(SPLITTER).length < 3){
				results.add(obj.toString());
			}
			else{
				String spliturl[] = obj.toString().split(SPLITTER);
				String parsedUrl = krameriusUrlParser(spliturl[2]);
				if(urlsMap.containsKey(parsedUrl)){
					List<String> completeUrls = urlsMap.get(parsedUrl);
					if(completeUrls.contains(obj.toString())) continue;
//					online url at the beginning
					if(spliturl[1].equals(ONLINE)) completeUrls.add(0, obj.toString());
					else completeUrls.add(obj.toString());
					urlsMap.put(parsedUrl, completeUrls);
				}
				else{
					List<String> list = new ArrayList<>();
					list.add(obj.toString());
					urlsMap.put(parsedUrl, list);
				}
			}
		}

		for(String url: urlsMap.keySet()){
			List<String> completeUrls = urlsMap.get(url);
			boolean online = false;
			List<String> unknownlist = new ArrayList<>();
			List<String> protectedlist = new ArrayList<>();
			for(String value: completeUrls){
				String spliturl[] = value.split(SPLITTER);
				if(spliturl[1].equals(ONLINE)){
					results.add(value);
					online = true;
				}
				else{
					if(online) break;
					if(spliturl[1].equals(PROTECTED)) protectedlist.add(value);
					if(spliturl[1].equals(UNKNOWN)) unknownlist.add(value);
				}
			}
			if (!protectedlist.isEmpty()) results.addAll(protectedlist);
			else {
				if(unknownlist.size() == 1) results.addAll(unknownlist);
				if(unknownlist.size() > 1){
					results.add(urlUpdaterAndJoiner(unknownlist.get(0), 0, UNKNOWN));
				}
			}
		}

		return results;
	}
	
	private String urlUpdaterAndJoiner(String url, int index, String newValue){
		String spliturl[] = url.split(SPLITTER);
		spliturl[index] = newValue;
		StringBuilder sb = new StringBuilder();
		sb.append(String.join(JOINER, spliturl));
		if(spliturl.length == 3) sb.append(JOINER);
		return sb.toString();
	}
	/**
	 * kramerius url formats
	 * http://kramerius.mzk.cz/search/i.jsp?pid=uuid:...
	 * http://kramerius.mzk.cz/search/handle/uuid:...
	 * @param url {@link String}
	 * @return parsed Url
	 */
	private String krameriusUrlParser(String url){
		Matcher matcher;
		if ((matcher = KRAM_MZK_PATTERN.matcher(url)).matches()) {
			return DIGITALNIKNIHOVNA + matcher.group(1);
		}
		if(url.contains(KRAMERIUS_URL)){
			return url.replace(KRAMERIUS_HANDLE, KRAMERIUS_IJSP);
		}
		
		return url;
	}
	
}
