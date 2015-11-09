package cz.mzk.recordmanager.server.scripting.dc;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.scripting.BaseDSL;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class DublinCoreDSL extends BaseDSL {

	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	private final DublinCoreRecord record;
	private MetadataDublinCoreRecord dcMetadataRecord;
	
	
	private final Map<String, RecordFunction<DublinCoreRecord>> functions;

	public DublinCoreDSL(DublinCoreRecord record,
			MappingResolver propertyResolver, StopWordsResolver stopWordsResolver,
			Map<String, RecordFunction<DublinCoreRecord>> functions) {
		super(propertyResolver, stopWordsResolver);
		this.record = record;
		this.functions = functions;
		this.dcMetadataRecord = new MetadataDublinCoreRecord(record);
	}

	public String getFirstTitle() {
		return record.getFirstTitle();
	}
	
	public String getFullRecord() {
		return record.getRawRecord() == null ? "" : new String(record.getRawRecord(), UTF8_CHARSET);
	}
	
	public String getRights() {
		List<String> rights = record.getRights();
		if (rights == null || rights.isEmpty()) {
			return Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
		}
		return rights.stream().anyMatch(s -> s.matches(".*public.*")) ? Constants.DOCUMENT_AVAILABILITY_ONLINE : Constants.DOCUMENT_AVAILABILITY_PROTECTED;
	}
	
	public List<String> getOtherTitles() {
		List<String> titles = record.getTitles();
		return (titles.size() <= 1) ? Collections.emptyList() : titles.subList(1, titles.size());
	}
	
	public String getFirstCreator() {
		return record.getFirstCreator();
	}
	
	public List<String> getOtherCreators() {
		List<String> creators = record.getCreators();
		List<String> contributors = record.getContributors();
		if (!creators.isEmpty()) {
			creators.remove(0); //removes first creator who goes to different field
		}
		if (!contributors.isEmpty()) {
			creators.addAll(contributors); //adds all contributors to other creators
		} 
		if (creators.isEmpty()) {
			return null;
		}
		return creators;
	}
	
	public String getFirstDate() {
		return record.getFirstDate();
	}
	
	public List <String> getPublishers() {
		return record.getPublishers();
	}
	
	public List <String> getSubjects() {
		return record.getSubjects();
	}
	
	public String getAllFields() {
		String result ="";
		if (!record.getCreators().isEmpty()) {result = result + record.getCreators().toString();}
		if (!record.getContributors().isEmpty()) {result = result + record.getContributors().toString();}
		if (!record.getLanguages().isEmpty()) {result = result + record.getLanguages().toString();}
		if (!record.getSubjects().isEmpty()) {result = result + record.getSubjects().toString();}
		if (!record.getTitles().isEmpty()) {result = result + record.getTitles().toString();}
		if (!record.getPublishers().isEmpty()) {result = result + record.getPublishers().toString();}
		if (!record.getDates().isEmpty()) {result = result + record.getDates().toString();}
		/* more to come..*/
//		System.out.println("getAllFields: " + result);
		return result;
	}
	
	public String getDescriptionText() {
		String result="";
		List<String> descriptions = record.getDescriptions();
		
		if (descriptions == null) {
			return null;
		} else {	
			for (String s: descriptions) {
				result += s;
			}
		}
		return result;
		
	}
	
	public String getPolicy() {
		return dcMetadataRecord.getPolicy();
	}
	
	public List<String> getISBNs() {
		List<Isbn> isbns = dcMetadataRecord.getISBNs();
		List<String> isbnsS = new ArrayList<String>();
		
	    for (Isbn n: isbns) {
	    	String isbn = n.getIsbn().toString();
	    	isbnsS.add(isbn);
	    }
	    return isbnsS;    
	}
	
	public List<String> getISSNs() {
		List<Issn> issns = dcMetadataRecord.getISSNs();
		List<String> issnsS = new ArrayList<String>();
		
	    for (Issn n: issns) {
	    	String issn = n.getIssn().toString();
	    	issnsS.add(issn);
	    }
	    return issnsS;    
	}
	


	public Object methodMissing(String methodName, Object args) {
		RecordFunction<DublinCoreRecord> func = functions.get(methodName);
		if (func == null) {
			throw new IllegalArgumentException(String.format("missing function: %s", methodName));
		}
		return func.apply(record, args);
	}
	
	public List<String> getStatuses() {
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, getRights());
	}

}
