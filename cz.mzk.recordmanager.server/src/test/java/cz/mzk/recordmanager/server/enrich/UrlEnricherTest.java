package cz.mzk.recordmanager.server.enrich;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.UrlDedupRecordEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;

public class UrlEnricherTest extends AbstractTest{

	private SolrInputDocument newField(String url){
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField(SolrFieldConstants.URL, url);
		return doc;
	}
	
	@Test
	public void notDuplicitUrlTest(){
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<SolrInputDocument>();
		local.add(newField("MZK|online|http://mzk.cz|"));
		local.add(newField("MZK|unknown|http://tre.cz|"));
		local.add(newField("MZK|protected|http://brno.cz|"));
		
		List<String> result = new ArrayList<>();
		result.add("MZK|online|http://mzk.cz|");
		result.add("MZK|unknown|http://tre.cz|");
		result.add("MZK|protected|http://brno.cz|");	

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);
		
		Assert.assertEquals(merged.getFieldValues(SolrFieldConstants.URL).toString(),
				result.toString());
	}
	
	@Test
	public void onlineUrlTest(){
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<SolrInputDocument>();
		local.add(newField("MZK|online|http://mzk.cz|"));
		local.add(newField("TRE|online|http://mzk.cz|"));
		local.add(newField("MZK|unknown|http://mzk.cz|"));
		local.add(newField("MZK|protected|http://mzk.cz|"));
		
		List<String> result = new ArrayList<>();
		result.add("TRE|online|http://mzk.cz|");
		result.add("MZK|online|http://mzk.cz|");

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);
		
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.URL).containsAll(result));
	}
	
	@Test
	public void unknownUrlTest(){
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<SolrInputDocument>();
		local.add(newField("MZK|unknown|http://mzk.cz|text"));
		local.add(newField("TRE|unknown|http://mzk.cz|text"));
		local.add(newField("MZK|unknown|http://brno.cz|"));
		local.add(newField("TRE|unknown|http://brno.cz|"));
		local.add(newField("MZK|unknown|http://tre.cz|"));
		
		List<String> result = new ArrayList<>();
		result.add("MZK|unknown|http://tre.cz|");
		result.add("unknown|unknown|http://mzk.cz|text");
		result.add("unknown|unknown|http://brno.cz|");

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);
		
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.URL).containsAll(result));
	}
	
	@Test
	public void unknownProtectedUrlTest(){
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<SolrInputDocument>();
		local.add(newField("TRE|unknown|http://mzk.cz|"));
		local.add(newField("MZK|protected|http://mzk.cz|"));
		
		List<String> result = new ArrayList<>();
		result.add("TRE|unknown|http://mzk.cz|");
		result.add("MZK|protected|http://mzk.cz|");	

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);
		
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.URL).containsAll(result));
	}
}
