package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusHarvesterNoSortingTest extends AbstractKrameriusTest {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SolrServerFactory solrServerFactory;

	//Kramerius Harvester with sorting
	@Test
	public void downloadRecord() throws Exception {
		init();
		KrameriusHarvesterParams parameters = new KrameriusHarvesterParams();
		parameters.setUrl("http://k4.techlib.cz/search/api/v5.0");
		parameters.setMetadataStream("DC");
		KrameriusHarvesterNoSorting harvester = new KrameriusHarvesterNoSorting(httpClient, solrServerFactory, parameters, 1L);
		List<String> uuids = harvester.getUuids(0);
		for (String uuid : uuids) {
			HarvestedRecord record = harvester.downloadRecord(uuid);
			Assert.assertNotNull(record);
		}
	}
	
	
	//Kramerius Harvester without sorting
	@Test
	public void harvesterNoServerResponse() throws Exception{
		initHttpClientWithException();
		initSolrServerWithException();
		KrameriusHarvesterParams parameters = new KrameriusHarvesterParams();
		parameters.setUrl("http://k4.techlib.cz/search/api/v5.0");
		parameters.setMetadataStream("DC");
		KrameriusHarvesterNoSorting harvester = new KrameriusHarvesterNoSorting(httpClient, solrServerFactory, parameters, 1L);
		
		//1st response with IOException => uuid list is empty
		List<String> uuids = harvester.getUuids(null);
		Assert.assertTrue(uuids.isEmpty());
		
		//2nd response with SolrServerException => uuid list is empty
		uuids = harvester.getUuids(0);
		Assert.assertTrue(uuids.isEmpty());

		//3rd response is => OK
		uuids = harvester.getUuids(0);
		Assert.assertFalse(uuids.isEmpty());

		//1st DC download - server responds with IOException, download returns null;
		HarvestedRecord record = harvester.downloadRecord(uuids.get(0));
		Assert.assertNull(record);
		
		//2nd DC download - OK
		record = harvester.downloadRecord(uuids.get(1));
		Assert.assertNotNull(record);
			
	}
	

}
