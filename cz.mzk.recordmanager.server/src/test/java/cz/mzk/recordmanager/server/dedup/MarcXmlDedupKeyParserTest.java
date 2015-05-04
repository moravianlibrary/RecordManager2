package cz.mzk.recordmanager.server.dedup;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;

public class MarcXmlDedupKeyParserTest extends AbstractTest {
	
	private static final Long EXPECTED_ISBN = 9788090539327L;
	private static final String EXPECTED_TITLE = "ceskarepublikamestaaobceceskerepublikytradicehistoriepamatkyturistikasoucasnost";
	
	@Autowired
	private MarcXmlDedupKeyParser parser;
	
	@Test
	public void parseCorrectRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-001439241.xml");
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(1L, "1");
		HarvestedRecord record = new HarvestedRecord(id );
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
		Assert.assertTrue(record.getIsbns().size() > 0);
		Assert.assertEquals(record.getIsbns().get(0).getIsbn(), EXPECTED_ISBN);
		Assert.assertEquals(record.getTitles().size(), 1);
		Assert.assertEquals(record.getTitles().get(0).getTitleStr(), EXPECTED_TITLE);
		Assert.assertEquals(record.getPhysicalFormat(), "Book");
		Assert.assertEquals(record.getPublicationYear(), new Long(2014));
	}
	
	@Test(expectedExceptions=InvalidMarcException.class)
	public void parseBadRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-000153226.xml");
		HarvestedRecordUniqueId id = new HarvestedRecordUniqueId(1L, "1");
		HarvestedRecord record = new HarvestedRecord(id);
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
	}

}
