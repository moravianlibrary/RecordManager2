package cz.mzk.recordmanager.server.kramerius.harvest;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusHarvesterTest extends AbstractKrameriusTest {

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
		KrameriusHarvester harvester = new KrameriusHarvester(httpClient, solrServerFactory, parameters, 1L);
		List<String> uuids = harvester.getUuids(null);
		for (String uuid : uuids) {
			HarvestedRecord record = harvester.downloadRecord(uuid);
			Assert.assertNotNull(record);
		}
	}

	//Kramerius Harvester without sorting
	@Test
	public void harvesterNoServerResponse() throws Exception {
		initHttpClientWithException();
		initSolrServerWithException();
		KrameriusHarvesterParams parameters = new KrameriusHarvesterParams();
		parameters.setUrl("http://k4.techlib.cz/search/api/v5.0");
		parameters.setMetadataStream("DC");
		KrameriusHarvester harvester = new KrameriusHarvester(httpClient, solrServerFactory, parameters, 1L);

		//1st response with SolrServerException => uuid list is empty
		try {
			harvester.getUuids(null);
			Assert.fail();
		} catch (SolrServerException ignore) {
		}

		//2nd response with SolrServerException => uuid list is empty
		try {
			harvester.getUuids(null);
			Assert.fail();
		} catch (SolrServerException ignore) {
		}

		//3rd response is => OK
		List<String> uuids = harvester.getUuids(null);
		Assert.assertFalse(uuids.isEmpty());

		//1st DC download - server responds with IOException, download returns null;
		try {
			harvester.downloadRecord(uuids.get(0));
			Assert.fail();
		} catch (IOException ignore) {
		}

		//2nd DC download - OK
		HarvestedRecord record = harvester.downloadRecord(uuids.get(1));
		Assert.assertNotNull(record);

	}


}
