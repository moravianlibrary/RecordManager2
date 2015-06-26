package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.List;

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

}
