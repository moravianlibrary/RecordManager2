package cz.mzk.recordmanager.server.kramerius.fulltext;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusFulltexterFedoraTest extends AbstractTest {

    private HttpClient httpClient = EasyMock.createMock(HttpClient.class);
	private static final String BASE_API_URL = "http://kramerius.mzk.cz/search/api/v5.0";  
	
	
	//positive
	@Test
	public void testPositive() throws Exception {
		String authToken = null;
		boolean downloadPrivateTexts=true;
		
		initPositive();
		KrameriusFulltexterFedora fedora = new KrameriusFulltexterFedora(BASE_API_URL, authToken, downloadPrivateTexts);
		fedora.setHttpClient(httpClient);
		
		List<FulltextKramerius> pages = fedora.getFulltextObjects("uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d");
		Assert.assertEquals(pages.size(), 2);
	}
	
	public void initPositive() throws Exception {
		reset(httpClient);
		InputStream json1 = getClass().getResourceAsStream("/sample/kramerius/children.json");
//		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d/children"), anyObject())).andReturn(json1);
		expect(httpClient.executeGet(BASE_API_URL + "/item/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d/children")).andReturn(json1);

		InputStream page1 = getClass().getResourceAsStream("/sample/kramerius/ocr1.txt");
		InputStream page2 = getClass().getResourceAsStream("/sample/kramerius/ocr2.txt");	
		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f5a22336-2fd8-11e0-83a8-0050569d679d/streams/TEXT_OCR"), anyObject())).andReturn(page1);
		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f64abf47-2fd8-11e0-83a8-0050569d679d/streams/TEXT_OCR"), anyObject())).andReturn(page2);
		replay(httpClient);		
	}

	//with invalid data
	@Test
	public void testNegative() throws Exception {
		String authToken = null;
		boolean downloadPrivateTexts=true;
		
		initNegative();
		KrameriusFulltexterFedora fedora = new KrameriusFulltexterFedora(BASE_API_URL, authToken, downloadPrivateTexts);
		fedora.setHttpClient(httpClient);
		
		//1st - server 500
		List<FulltextKramerius> pages = fedora.getFulltextObjects("uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d");
		Assert.assertEquals(pages.size(), 0);

		//2nd - not JSON
		pages = fedora.getFulltextObjects("uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d");
		Assert.assertEquals(pages.size(), 0);
		
		//3rd - JSON is valid, but makes no sense (fields are wrong)
		pages = fedora.getFulltextObjects("uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d");
		Assert.assertEquals(pages.size(), 0);
		
		//4th - JSON is OK, but fails to download one OCR
		pages = fedora.getFulltextObjects("uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d");
		// we expect to have 2 pages
		Assert.assertEquals(pages.size(), 2);
		// fulltext from first page is null (server returned 500)
		Assert.assertNull(pages.get(0).getFulltext());
		// fulltext from second page is not null (server returned text)
		Assert.assertNotNull(pages.get(1).getFulltext());
	}
	
	public void initNegative() throws Exception {
		reset(httpClient);
		//1st server 500
		expect(httpClient.executeGet(BASE_API_URL + "/item/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d/children")).andThrow(new IOException("Bad status code: 500"));

		//2nd not JSON
		InputStream notJson = getClass().getResourceAsStream("/sample/kramerius/ocr1.txt");
		expect(httpClient.executeGet(BASE_API_URL + "/item/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d/children")).andReturn(notJson);

		//3rd  JSON is valid, but makes no sense (fields are wrong)
		InputStream anotherJson = getClass().getResourceAsStream("/sample/kramerius/invalidJson.json");
		expect(httpClient.executeGet(BASE_API_URL + "/item/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d/children")).andReturn(anotherJson);
		
		//4th correct JSON
		InputStream json3 = getClass().getResourceAsStream("/sample/kramerius/children.json");
		expect(httpClient.executeGet(BASE_API_URL + "/item/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d/children")).andReturn(json3);

		//InputStream page1 = getClass().getResourceAsStream("/sample/kramerius/ocr1.txt");
		InputStream page2 = getClass().getResourceAsStream("/sample/kramerius/ocr2.txt");	
		// return 500 for one page
		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f5a22336-2fd8-11e0-83a8-0050569d679d/streams/TEXT_OCR"), anyObject())).andThrow(new IOException("Bad status code: 500"));;
		expect(httpClient.executeGet(eq(BASE_API_URL + "/item/uuid:f64abf47-2fd8-11e0-83a8-0050569d679d/streams/TEXT_OCR"), anyObject())).andReturn(page2);
		replay(httpClient);		
	}
	
}
