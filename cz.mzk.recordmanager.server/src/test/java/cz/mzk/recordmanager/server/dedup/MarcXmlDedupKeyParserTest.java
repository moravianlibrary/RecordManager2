package cz.mzk.recordmanager.server.dedup;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordId;

public class MarcXmlDedupKeyParserTest extends AbstractTest {
	
	private static final String EXPECTED_ISBN = "9788090539327";
	private static final String EXPECTED_TITLE = "ceskarepublikamestaaobceceskerepublikytradicehistoriepamatkyturistikasoucasnost";
	
	@Autowired
	private MarcXmlDedupKeyParser parser;
	
	@Test
	public void parseCorrectRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-001439241.xml");
		HarvestedRecordId id = new HarvestedRecordId(1L, "1");
		HarvestedRecord record = new HarvestedRecord(id );
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
		Assert.assertEquals(record.getIsbn(), EXPECTED_ISBN);
		Assert.assertEquals(record.getTitle(), EXPECTED_TITLE);
		Assert.assertEquals(record.getPhysicalFormat(), "Book");
		Assert.assertEquals(record.getPublicationYear(), new Long(2014));
	}
	
	@Test(expectedExceptions=InvalidMarcException.class)
	public void parseBadRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-000153226.xml");
		HarvestedRecordId id = new HarvestedRecordId(1L, "1");
		HarvestedRecord record = new HarvestedRecord(id);
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
	}

}
