package cz.mzk.recordmanager.server.scripting;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.dc.DublinCoreRecordImpl;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreFunctionContext;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreScriptFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Map;

public class DublinCoreScriptFactoryTest extends AbstractTest {
	
	@Autowired
	private DublinCoreScriptFactory dcScriptFactory;

	@Autowired
	private MetadataRecordFactory mrFactory;
	
	@Test
	public void test() {
		InputStream is1 = getClass().getResourceAsStream(
				"/groovy/BaseDublinCore.groovy");
		MappingScript<DublinCoreFunctionContext> script = dcScriptFactory.create(is1);
		DublinCoreRecord dcRecord = new DublinCoreRecordImpl();
		dcRecord.addTitle("test");
		MetadataRecord mr = mrFactory.getMetadataRecord(dcRecord);
		DublinCoreFunctionContext dcContext = new DublinCoreFunctionContext(dcRecord, mr);
		Map<String, Object> entries = script.parse(dcContext);
		Assert.assertEquals(entries.size(), 1);
		Assert.assertEquals(entries.get("title"), "test");
	}

}
