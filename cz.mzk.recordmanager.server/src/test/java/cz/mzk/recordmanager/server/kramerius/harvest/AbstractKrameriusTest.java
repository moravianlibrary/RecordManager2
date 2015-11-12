package cz.mzk.recordmanager.server.kramerius.harvest;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.io.InputStream;
import java.util.List;

import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl.Mode;
import cz.mzk.recordmanager.server.util.HttpClient;

public class AbstractKrameriusTest extends AbstractTest {

	private static final String SOLR_URL = "http://k4.techlib.cz/search/api/v5.0";

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private SolrServerFactory solrServerFactory;

	private SolrServerFacade mockedSolrServer = EasyMock.createMock(SolrServerFacade.class);

	protected void init() throws Exception {
		reset(httpClient);
		InputStream response1 = this.getClass().getResourceAsStream("/sample/kramerius/DownloadItem1.xml");
		InputStream response2 = this.getClass().getResourceAsStream("/sample/kramerius/DownloadItem2.xml");
		expect(httpClient.executeGet("http://k4.techlib.cz/search/api/v5.0/item/uuid:00931210-02b6-11e5-b939-0800200c9a66/streams/DC")).andReturn(response1);
		expect(httpClient.executeGet("http://k4.techlib.cz/search/api/v5.0/item/uuid:0095bca0-614f-11e2-bcfd-0800200c9a66/streams/DC")).andReturn(response2);
		replay(httpClient);
		
		reset(solrServerFactory);
		expect(solrServerFactory.create(eq(SOLR_URL), eq(Mode.KRAMERIUS))).andReturn(mockedSolrServer).anyTimes();
		replay(solrServerFactory);
		
		reset(mockedSolrServer);
		Capture<QueryRequest> capturedQueryRequest = EasyMock.newCapture();
		SolrDocumentList documents = new SolrDocumentList();
		SolrDocument doc1 = new SolrDocument();
		doc1.addField("PID", "uuid:00931210-02b6-11e5-b939-0800200c9a66");
		SolrDocument doc2 = new SolrDocument();
		doc2.addField("PID", "uuid:0095bca0-614f-11e2-bcfd-0800200c9a66");
		documents.add(doc1);
		documents.add(doc2);
		NamedList<Object> solrResponse1 = new NamedList<Object>();
		solrResponse1.add("response", documents);
		expect(mockedSolrServer.query(and(capture(capturedQueryRequest), anyObject(QueryRequest.class)))).andReturn(new QueryResponse(solrResponse1, null));
		NamedList<Object> solrResponse2 = new NamedList<Object>();
		solrResponse2.add("response", new SolrDocumentList());
		expect(mockedSolrServer.query(and(capture(capturedQueryRequest), anyObject(QueryRequest.class)))).andReturn(new QueryResponse(solrResponse2, null));
		replay(mockedSolrServer);
		
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
