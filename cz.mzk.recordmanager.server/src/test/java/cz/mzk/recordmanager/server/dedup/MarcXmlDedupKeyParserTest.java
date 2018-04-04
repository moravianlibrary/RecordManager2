package cz.mzk.recordmanager.server.dedup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

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
	private static final String EXPECTED_SHORT_TITLE = "ceskarepublika";
	private static final String EXPECTED_AUTHORAUTHKEY = "jo2012735774";
	private static final String EXPECTED_AUTHORSTRING = "sevcikovaveronika";
	private static final Long EXPECTED_SCALE = null;
	private static final String EXPECTED_UUID = null;
	private static final String EXPECTED_ISSNSERIES = null;
	private static final String EXPECTED_ISSNSERIESORDER = null;
	private static final String EXPECTED_001 = "nkc20142639106";
	private static final Long EXPECTED_WEIGHT = 17L;
	private static final String EXPECTED_NULL = null;
	private static final Long EXPECTED_PAGE_COUNT = 1519L;
	private static final String EXPECTED_CNB = "cnb002639106";
	private static final String EXPECTED_LANG = "cze";
	
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
		HarvestedRecord record = parseRecord("/records/marcxml/MZK01-001439241.xml");
		Assert.assertTrue(record.getIsbns().size() > 0);
		Assert.assertEquals(record.getIsbns().get(0).getIsbn(), EXPECTED_ISBN);
		Assert.assertEquals(record.getTitles().size(), 1);
		Assert.assertEquals(record.getTitles().get(0).getTitleStr(), EXPECTED_TITLE);
		Assert.assertEquals(record.getShortTitles().size(), 1);
		Assert.assertEquals(record.getShortTitles().get(0).getShortTitleStr(), EXPECTED_SHORT_TITLE);
		Assert.assertEquals(record.getPhysicalFormats().size(), 1);
		Assert.assertEquals(record.getPhysicalFormats().get(0).getName(), HarvestedRecordFormatEnum.BOOKS.name());
		Assert.assertEquals(record.getPublicationYear(), new Long(2014));
		Assert.assertEquals(record.getAuthorAuthKey(), EXPECTED_AUTHORAUTHKEY);
		Assert.assertEquals(record.getAuthorString(), EXPECTED_AUTHORSTRING);
		Assert.assertEquals(record.getScale(), EXPECTED_SCALE);
		Assert.assertEquals(record.getUuid(), EXPECTED_UUID);
		Assert.assertEquals(record.getIssnSeries(), EXPECTED_ISSNSERIES);
		Assert.assertEquals(record.getIssnSeriesOrder(), EXPECTED_ISSNSERIESORDER);
		Assert.assertEquals(record.getRaw001Id(), EXPECTED_001);
		Assert.assertEquals(record.getWeight(), EXPECTED_WEIGHT);
		Assert.assertEquals(record.getClusterId(), EXPECTED_NULL);
		Assert.assertEquals(record.getPages(), EXPECTED_PAGE_COUNT);
		Assert.assertEquals(record.getCnb().size(), 1);
		Assert.assertEquals(record.getCnb().get(0).getCnb(), EXPECTED_CNB);
		Assert.assertEquals(record.getLanguages(), Collections.singleton(EXPECTED_LANG));
	}

	private static final String EXPECTED_SOURCE_INFO_G = "sv222008s365377407408";
	private static final String EXPECTED_SOURCE_INFO_T = "brnovminulostiadnes";
	private static final String EXPECTED_SOURCE_INFO_X = "0524689x";

	@Test
	public void parseArcticle() throws IOException {
		HarvestedRecord record = parseRecord("/records/marcxml/MZK01-001101044.xml");
		Assert.assertEquals(record.getPhysicalFormats().get(0).getName(), HarvestedRecordFormatEnum.ARTICLES.name());
		Assert.assertEquals(record.getSourceInfoG(), EXPECTED_SOURCE_INFO_G);
		Assert.assertEquals(record.getSourceInfoT(), EXPECTED_SOURCE_INFO_T);
		Assert.assertEquals(record.getSourceInfoX(), EXPECTED_SOURCE_INFO_X);
	}

	private static final Long EXPECTED_EAN = 8595082723418L;
	private static final String EXPECTED_PUBLISHER_NUMBER = "av0341122";

	@Test
	public void parseAudio() throws IOException {
		HarvestedRecord record = parseRecord("/records/marcxml/MZK01-001230956.xml");
		Assert.assertEquals(record.getEans().size(), 1);
		Assert.assertEquals(record.getEans().get(0).getEan(), EXPECTED_EAN);
		Assert.assertEquals(record.getPublisherNumbers().size(), 1);
		Assert.assertEquals(record.getPublisherNumbers().get(0).getPublisherNumber(), EXPECTED_PUBLISHER_NUMBER);
	}

	private static final String EXPECTED_ISSN = "1212-1029";

	@Test
	public void parsePeridical() throws IOException {
		HarvestedRecord record = parseRecord("/records/marcxml/MZK01-000535234.xml");
		Assert.assertEquals(record.getIssns().size(), 1);
		Assert.assertEquals(record.getIssns().get(0).getIssn(), EXPECTED_ISSN);
	}

	private static final Long EXPECTED_ISMN = 9790260107489L;

	@Test
	public void parseMusicalScore() throws IOException {
		HarvestedRecord record = parseRecord("/records/marcxml/MZK01-001565745.xml");
		Assert.assertEquals(record.getIsmns().size(), 1);
		Assert.assertEquals(record.getIsmns().get(0).getIsmn(), EXPECTED_ISMN);
	}

	private HarvestedRecord parseRecord(final String fileName) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(fileName);
		ImportConfiguration ic = importConfDao.get(300L);
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(ic, "1");
		HarvestedRecord record = new HarvestedRecord(id);
		record.setHarvestedFrom(ic);
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		record.setId(1L);
		parser.parse(record);
		return record;
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
