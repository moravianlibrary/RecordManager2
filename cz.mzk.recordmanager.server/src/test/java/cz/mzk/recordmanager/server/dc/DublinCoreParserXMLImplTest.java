package cz.mzk.recordmanager.server.dc;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;

public class DublinCoreParserXMLImplTest extends AbstractTest {

	@Autowired
	private DublinCoreParser parser;

	@Test
	public void testDCRecordK4NTK() {
		InputStream is = this
				.getClass()
				.getResourceAsStream(
						"/records/dublincore/K4NTK-uuid-d3cf7ce9-1891-4e39-bb35-3b38b6eb0d60.xml");
		DublinCoreRecord dc = parser.parseRecord(is);
		Assert.assertNotNull(dc);
		
		//Kramerius specific tests
		MetadataDublinCoreRecord mdrc = new MetadataDublinCoreRecord(dc);
		Assert.assertTrue(mdrc.getPolicyKramerius().equals("public"));
		Assert.assertTrue(mdrc.getModelKramerius().equals("monograph"));
		
	}

	@Test
	public void testDCRecordK4MZK() {
		InputStream is = this
				.getClass()
				.getResourceAsStream(
						"/records/dublincore/K4MZK-uuid-c28962c9-a67a-11e1-ac9a-0050569d679d.xml");
		DublinCoreRecord dc = parser.parseRecord(is);
		Assert.assertNotNull(dc);

	}

	@Test
	public void testDCRecordNuslNTK() {
		/* System.out.println("test NuslNTK - parser"); */
		/* file contains complete oai-pmh metadata structure */
		InputStream is = this.getClass().getResourceAsStream(
				"/records/dublincore/NuslNTK-nusl2-oaipmh.xml");
		DublinCoreRecord dc = parser.parseRecord(is);
		Assert.assertNotNull(dc);

		//Kramerius specific data are not present
		MetadataDublinCoreRecord mdrc = new MetadataDublinCoreRecord(dc);
		Assert.assertTrue(mdrc.getModelKramerius().equals("unknown"));
	}
	
	@Test
	public void testDCRecordManuscriptorium() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/dublincore/manuscriptorium.xml");
		DublinCoreRecord dc = parser.parseRecord(is);
		Assert.assertNotNull(dc);
		Assert.assertEquals(dc.getFirstIdentifier(), "AIPDIG-AMBFSK36_73_______02ZG1F9-cs");
	}

	//test of file which is not even XML 
	@Test(expectedExceptions = InvalidDcException.class)
	public void testNotXMLDCRecord() {
		InputStream is = this
				.getClass()
				.getResourceAsStream(
						"/sample/kramerius/children.json");
		parser.parseRecord(is);

	}
	
	
}
