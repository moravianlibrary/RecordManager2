package cz.mzk.recordmanager.server.dc;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;

public class DublinCoreParserXMLImplTest extends AbstractTest {

	@Autowired
	private DublinCoreParser parser;
	
	@Test
	public void testDCRecordK4NTK() {
		System.out.println("test K4NTK- parser");
		InputStream is = this.getClass().getResourceAsStream(
				"/records/dublincore/K4NTK-uuid-d3cf7ce9-1891-4e39-bb35-3b38b6eb0d60.xml");
/*		System.out.println("načetl se soubor..");*/
		DublinCoreRecord dc = parser.parseRecord(is);
/*
		System.out.println("v zaznamu je title: "+dc.getFirstTitle());
		System.out.println("v záznamu je creator: "+dc.getFirstCreator());
		System.out.println("v záznamu je identifier: "+dc.getFirstIdentifier());*/
		Assert.assertNotNull(dc);
	
	}
	
	
	@Test
	public void testDCRecordK4MZK() {
		System.out.println("test K4MZK- parser");
		InputStream is = this.getClass().getResourceAsStream(
				"/records/dublincore/K4MZK-uuid-c28962c9-a67a-11e1-ac9a-0050569d679d.xml");
/*		System.out.println("načetl se soubor.."); */
		DublinCoreRecord dc = parser.parseRecord(is);

/*		System.out.println("v zaznamu je title: "+dc.getFirstTitle());
		System.out.println("v záznamu je creator: "+dc.getFirstCreator());
		System.out.println("v záznamu je identifier: "+dc.getFirstIdentifier()); */
		Assert.assertNotNull(dc);
	
	}
	
	@Test
	public void testDCRecordNuslNTK() {
/*		System.out.println("test NuslNTK - parser");*/ 
		/*file contains complete oai-pmh metadata structure */
		InputStream is = this.getClass().getResourceAsStream(
				"/records/dublincore/NuslNTK-nusl2-oaipmh.xml");
		System.out.println("načetl se soubor..");
		DublinCoreRecord dc = parser.parseRecord(is);

/*		System.out.println("v zaznamu je title: "+dc.getFirstTitle());
		System.out.println("v záznamu je creator: "+dc.getFirstCreator());
		System.out.println("v záznamu je identifier: "+dc.getFirstIdentifier());*/
		Assert.assertNotNull(dc);
	
	}
	
}
