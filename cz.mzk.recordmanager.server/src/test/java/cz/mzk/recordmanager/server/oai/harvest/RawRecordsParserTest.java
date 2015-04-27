package cz.mzk.recordmanager.server.oai.harvest;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.oai.model.OAIRoot;

public class RawRecordsParserTest {
	
	@Test
	public void parse() throws Exception {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(OAIRoot.class);
		marshaller.afterPropertiesSet();
		InputStream is = this.getClass().getResourceAsStream("/sample/ListRecords1.xml");
		OAIRoot result = (OAIRoot) marshaller.unmarshal(new StreamSource(is));
		Assert.assertNotNull(result.getRequest());
		Assert.assertNotNull(result.getRequest().getVerb());
		Assert.assertNotNull(result.getRequest().getResumptionToken());
		String expectedResumptionToken = "201408211302186999999999999999MZK01-VDK:MZK01-VDK";
		Assert.assertEquals(result.getListRecords().getNextResumptionToken(), expectedResumptionToken);
		Assert.assertTrue(result.getListRecords().getRecords().size() > 0);
	}

}
