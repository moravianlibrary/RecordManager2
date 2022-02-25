package cz.mzk.recordmanager.server.index;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.resetToNice;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.solr.FaultTolerantIndexingExceptionHandler;
import cz.mzk.recordmanager.server.solr.SolrIndexingExceptionHandler;
import cz.mzk.recordmanager.server.solr.SolrServerFacadeImpl;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl.Mode;

public class AbstractSolrTest extends AbstractTest {

	@Autowired
	protected SolrServerFactory solrServerFactory;

	@Autowired
	private SolrClient server;

	protected static final String SOLR_URL = "http://localhost:8080/solr";

	@BeforeMethod
	protected void createEmbeddedSolrServer() throws Exception {
		server.deleteByQuery("*:*");
		server.commit();
		resetToNice(solrServerFactory);
		SolrIndexingExceptionHandler handler = new FaultTolerantIndexingExceptionHandler();
		expect(solrServerFactory.create(eq(SOLR_URL), anyObject(Mode.class), anyObject(SolrIndexingExceptionHandler.class))).andReturn(new SolrServerFacadeImpl(server, handler)).anyTimes();
		expect(solrServerFactory.create(eq(SOLR_URL))).andReturn(new SolrServerFacadeImpl(server, handler)).anyTimes();
		replay(solrServerFactory);
	}

}
