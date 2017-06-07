package cz.mzk.recordmanager.server.dedup;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;

public class MarcXmlDedupKeyParserTest extends AbstractTest {
	
	private static final Long EXPECTED_ISBN = 9788090539327L;
	private static final String EXPECTED_TITLE = "ceskarepublikamestaaobceceskerepublikytradicehistoriepamatkyturistikasoucasnost";
	private static final String EXPECTED_AUTHORAUTHKEY = "jo2012735774";
	private static final String EXPECTED_AUTHORSTRING = "sevcikovaveronika";
	private static final Long EXPECTED_SCALE = null;
	private static final String EXPECTED_UUID = null;
	private static final String EXPECTED_ISSNSERIES = null;
	private static final String EXPECTED_ISSNSERIESORDER = null;
	
	@Autowired
	private MarcXmlDedupKeyParser parser;

	@Autowired
	private ImportConfigurationDAO importConfDao;

	@BeforeMethod
	public void initDb() throws Exception {
		dbUnitHelper.init("dbunit/ImportRecords.xml");
	}

	@Test
	public void parseCorrectRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-001439241.xml");
		ImportConfiguration ic = importConfDao.get(300L);
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(ic, "1");
		HarvestedRecord record = new HarvestedRecord(id);
		record.setHarvestedFrom(ic);
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		record.setId(1L);
		parser.parse(record);
		Assert.assertTrue(record.getIsbns().size() > 0);
		Assert.assertEquals(record.getIsbns().get(0).getIsbn(), EXPECTED_ISBN);
		Assert.assertEquals(record.getTitles().size(), 1);
		Assert.assertEquals(record.getTitles().get(0).getTitleStr(), EXPECTED_TITLE);
		Assert.assertEquals(record.getPhysicalFormats().size(), 1);
		Assert.assertEquals(record.getPhysicalFormats().get(0).getName(), HarvestedRecordFormatEnum.BOOKS.name());
		Assert.assertEquals(record.getPublicationYear(), new Long(2014));
		Assert.assertEquals(record.getAuthorAuthKey(), EXPECTED_AUTHORAUTHKEY);
		Assert.assertEquals(record.getAuthorString(), EXPECTED_AUTHORSTRING);
		Assert.assertEquals(record.getScale(), EXPECTED_SCALE);
		Assert.assertEquals(record.getUuid(), EXPECTED_UUID);
		Assert.assertEquals(record.getIssnSeries(), EXPECTED_ISSNSERIES);
		Assert.assertEquals(record.getIssnSeriesOrder(), EXPECTED_ISSNSERIESORDER);
	}
	
//	@Test(expectedExceptions=InvalidMarcException.class)
//	public void parseBadRecord() throws Exception {
//		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-000153226.xml");
//		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(1L, "1");
//		HarvestedRecord record = new HarvestedRecord(id);
//		record.setFormat("marc21-xml");
//		byte[] rawRecord = ByteStreams.toByteArray(is);
//		record.setRawRecord(rawRecord);
//		parser.parse(record);
//	}

}
