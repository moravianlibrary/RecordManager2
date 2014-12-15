package cz.mzk.recordmanager.server.oai.harvest;

import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.oai.model.OAIListRecords;

public class RawRecordsParserTest {
	
	@Test
	public void parse() throws Exception {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(OAIListRecords.class);
		marshaller.afterPropertiesSet();
		OAIListRecords result = (OAIListRecords) marshaller.unmarshal(new StreamSource(this.getClass().getResourceAsStream("/sample/ListRecords.xml")));
		Assert.assertNotNull(result.getRequest());
		Assert.assertNotNull(result.getRequest().getVerb());
		Assert.assertNotNull(result.getRequest().getResumptionToken());
		Assert.assertEquals(result.getNextResumptionToken(), "201408211302186999999999999999MZK01-VDK:MZK01-VDK");
		Assert.assertTrue(result.getRecords().size() > 0);
	}

}
