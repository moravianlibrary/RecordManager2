package cz.mzk.recordmanager.server.marc;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;

/**
 * Test various marcxml samples
 */
public class MarcXmlParserTest extends AbstractTest {

	@Autowired
	private MarcXmlParser parser;
	
	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Test
	public void testMZKRecordMZK() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/MZK01-001439241.xml");
		MarcRecord marc = parser.parseRecord(is);
		MetadataRecord metadataRecord = metadataFactory.getMetadataRecord(marc);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("040", 'a'), "BOA001");
		final String expectedTitle = "Česká republika : města a obce "
				+ "České republiky : tradice, historie, památky, "
				+ "turistika, současnost /";
		Assert.assertEquals(metadataRecord.getTitle(), expectedTitle);

		List<String> fields650 = marc.getFields("650", " ", new char[] { 'a',
				'z' });
		Assert.assertEquals(fields650.size(), 4);
		Assert.assertTrue(fields650.contains("obce Česko"));
		Assert.assertEquals(metadataRecord.getPublicationYear(), new Long(2014));
		Assert.assertEquals(metadataRecord.getFormat(), "Book");

		
		DataFieldMatcher matcher = field -> field.getIndicator1() == ' '
				&& field.getIndicator2() == '7';
		marc.getFields("072", matcher, " ", 'x');
	}

	@Test
	public void testRecordKFBZ() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/KFBZ-kpw0120405.xml");
		MarcRecord marc = parser.parseRecord(is);
		MetadataRecord metadataRecord = metadataFactory.getMetadataRecord(marc);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("020", 'a'), "80-200-0358-4");
		final String expectedTitle = "Česká nedělní postila = Postilla "
				+ "de tempore Bohemica : vyloženie svatých čtení nedělních /";
		Assert.assertEquals(metadataRecord.getPublicationYear(), new Long(1992));
		Assert.assertEquals(metadataRecord.getFormat(), "Book");
		Assert.assertEquals(metadataRecord.getTitle(), expectedTitle);
	}

	/**
	 * test record with Alphanumeric field names
	 */
	@Test
	public void testRecordNLK() {
		InputStream is = this.getClass().getResourceAsStream(
				"/records/marcxml/NLK-192047.xml");
		MarcRecord marc = parser.parseRecord(is);
		MetadataRecord metadataRecord = metadataFactory.getMetadataRecord(marc);
		Assert.assertNotNull(marc);
		Assert.assertEquals(marc.getField("020", 'a'), "3-540-08474-6");
		Assert.assertEquals(marc.getField("HGS", 'a'), "0");
		Assert.assertEquals(metadataRecord.getPublicationYear(), new Long(1978));
		Assert.assertEquals(metadataRecord.getFormat(), "Book");
		final String expectedTitle = "Cardiomyopathy and myocardial biopsy /";
		Assert.assertEquals(metadataRecord.getTitle(), expectedTitle);
	}

}
