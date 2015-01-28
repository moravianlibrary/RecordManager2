package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;

/**
 * Test various marcxml samples
 */
public class MarcXmlParserTest extends AbstractTest {

	@Autowired
	private MarcXmlParser parser;

	@Test
	public void testMZKRecordMZK() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/MZK01-001439241.xml");
		MarcRecord marc = parser.parseRecord(is);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("040", 'a'), "BOA001");
	}

	@Test
	public void testRecordKFBZ() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/KFBZ-kpw0120405.xml");
		MarcRecord marc = parser.parseRecord(is);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("020", 'a'), "80-200-0358-4");
	}

	/**
	 * test record with Alphanumeric field names
	 */
	@Test
	public void testRecordNLK() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/NLK-192047.xml");
		MarcRecord marc = parser.parseRecord(is);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("020", 'a'), "3-540-08474-6");
		Assert.assertEquals(marc.getField("HGS", 'a'), "0");
	}

}
