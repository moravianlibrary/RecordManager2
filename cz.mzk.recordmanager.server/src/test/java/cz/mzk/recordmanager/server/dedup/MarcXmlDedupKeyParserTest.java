package cz.mzk.recordmanager.server.dedup;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public class MarcXmlDedupKeyParserTest extends AbstractTest {
	
	private static final String EXPECTED_ISBN = "978-80-905393-2-7";
	
	@Autowired
	private MarcXmlDedupKeyParser parser;
	
	@Test
	public void parse() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/sample/MarcXmlRecord.xml");
		HarvestedRecord record = new HarvestedRecord();
		record.setFormat("marc21-xml");
		byte[] rawRecord = ByteStreams.toByteArray(is);
		record.setRawRecord(rawRecord);
		parser.parse(record);
		Assert.assertEquals(record.getIsbn(), EXPECTED_ISBN);
	}

}
