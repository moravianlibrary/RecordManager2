package cz.mzk.recordmanager.server.facade;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.capture;

import java.io.IOException;
import java.io.InputStream;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;

public class HarvestingFacadeTest extends AbstractTest {

	@Autowired
	private OAIHarvestConfigurationDAO oaiHarvestConfigurationDao;

	@Autowired
	private HarvestingFacade harvestingFacade;

	@Autowired
	private HttpClient httpClient;

	@BeforeMethod
	public void initLocator() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}

	@Test
	public void harvest() throws IOException {
		reset(httpClient);
		final Capture<String> url = EasyMock.newCapture();
		expect(httpClient.executeGet(capture(url))).andAnswer(() -> reply(url.getValue())).anyTimes();
		replay(httpClient);
		
		OAIHarvestConfiguration conf = oaiHarvestConfigurationDao.get(300L);
		harvestingFacade.incrementalHarvest(conf);
		harvestingFacade.incrementalHarvest(conf);
	}

	public InputStream reply(String url) {
		if (url.contains("verb=Identify")) {
			return getClass().getResourceAsStream("/sample/Identify.xml");
		} else {
			return getClass().getResourceAsStream("/sample/ListRecords2.xml");
		}
	}

}
