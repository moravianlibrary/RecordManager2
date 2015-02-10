package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;

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
		final String expectedTitle = "Česká republika : města a obce "
				+ "České republiky : tradice, historie, památky, "
				+ "turistika, současnost /";
		Assert.assertEquals(marc.getTitle(), expectedTitle);
		List<String> fields650 = marc.getFields("650", " ", new char[] {'a', 'z'});
		Assert.assertEquals(fields650.size(), 4);
		Assert.assertTrue(fields650.contains("obce Česko"));
	}

	@Test
	public void testRecordKFBZ() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/KFBZ-kpw0120405.xml");
		MarcRecord marc = parser.parseRecord(is);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("020", 'a'), "80-200-0358-4");
		final String expectedTitle = "Česká nedělní postila = Postilla "
				+ "de tempore Bohemica : vyloženie svatých čtení nedělních /";
		Assert.assertEquals(marc.getTitle(), expectedTitle);
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
		final String expectedTitle = "Cardiomyopathy and myocardial biopsy /";
		Assert.assertEquals(marc.getTitle(), expectedTitle);
	}

}
