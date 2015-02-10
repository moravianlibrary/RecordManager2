package cz.mzk.recordmanager.server.dedup;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class MarcXmlDedupKeyParserTest extends AbstractTest {
	
	private static final String EXPECTED_ISBN = "9788090539327";
	private static final String EXPECTED_TITLE = "ceskarepublikamestaaobceceskerepublikytradicehistoriepamatkyturistikasoucasnost";
	
	@Autowired
	private MarcXmlDedupKeyParser parser;
	
	@Test
	public void parseCorrectRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-001439241.xml");
		HarvestedRecord record = new HarvestedRecord();
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
		Assert.assertEquals(record.getIsbn(), EXPECTED_ISBN);
		Assert.assertEquals(record.getTitle(), EXPECTED_TITLE);
	}
	
	@Test(expectedExceptions=DedupKeyParserException.class)
	public void parseBadRecord() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/records/marcxml/MZK01-000153226.xml");
		HarvestedRecord record = new HarvestedRecord();
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
	}

}
