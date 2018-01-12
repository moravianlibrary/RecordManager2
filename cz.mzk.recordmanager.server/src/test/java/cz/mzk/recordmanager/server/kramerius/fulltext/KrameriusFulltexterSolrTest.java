package cz.mzk.recordmanager.server.kramerius.fulltext;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;

public class KrameriusFulltexterSolrTest extends AbstractTest {

	private SolrServerFacade mockedSolrServer = EasyMock.createMock(SolrServerFacade.class);

	@Test
	public void test() throws Exception {
		prepare();
		KrameriusFulltexterSolr solr = new KrameriusFulltexterSolr(mockedSolrServer);
		List<FulltextKramerius> pages = solr.getFulltextObjects("uuid:111ad136-1f0a-4cd0-9d00-242155955bdc");
		Assert.assertEquals(pages.size(), 3);
	}

	public void prepare() throws Exception {
		Capture<SolrQuery> capturedQueryRequest = EasyMock.newCapture();
		SolrDocumentList documents = new SolrDocumentList();

		SolrDocument pageA = new SolrDocument();
		pageA.addField(KrameriusSolrConstants.PID_FIELD, "uuid:00931210-02b6-11e5-b939-0800200c9a66");
		pageA.addField(KrameriusSolrConstants.FULLTEXT_FIELD, "stranka A");
		pageA.addField(KrameriusSolrConstants.PAGE_NUMBER_FIELD, "[A]");
		pageA.addField(KrameriusSolrConstants.PAGE_ORDER_FIELD, "1A");

		SolrDocument page1 = new SolrDocument();
		page1.addField(KrameriusSolrConstants.PID_FIELD, "uuid:0095bca0-614f-11e2-bcfd-0800200c9a66");
		page1.addField(KrameriusSolrConstants.FULLTEXT_FIELD, "stranka 1");
		page1.addField(KrameriusSolrConstants.PAGE_NUMBER_FIELD, "[1]");
		page1.addField(KrameriusSolrConstants.PAGE_ORDER_FIELD, "1");

		SolrDocument page2 = new SolrDocument();
		page2.addField(KrameriusSolrConstants.PID_FIELD, "uuid:0095bca0-614f-11e2-bcfd-0800200c9a66");
		page2.addField(KrameriusSolrConstants.FULLTEXT_FIELD, "stranka 2");
		page2.addField(KrameriusSolrConstants.PAGE_NUMBER_FIELD, "[2]");
		page2.addField(KrameriusSolrConstants.PAGE_ORDER_FIELD, "2");

		documents.add(pageA);
		documents.add(page1);
		documents.add(page2);
		NamedList<Object> solrResponse1 = new NamedList<Object>();
		solrResponse1.add("response", documents);
		expect(mockedSolrServer.query(and(capture(capturedQueryRequest), anyObject(SolrQuery.class)))).andReturn(new QueryResponse(solrResponse1, null));
		replay(mockedSolrServer);
	}

}
