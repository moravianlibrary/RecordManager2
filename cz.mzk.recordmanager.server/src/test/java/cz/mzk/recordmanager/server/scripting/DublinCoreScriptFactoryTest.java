package cz.mzk.recordmanager.server.scripting;

import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.dc.DublinCoreRecordImpl;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreFunctionContext;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreScriptFactory;

public class DublinCoreScriptFactoryTest extends AbstractTest {
	
	@Autowired
	private DublinCoreScriptFactory factory;
	
	@Test
	public void test() {
		InputStream is1 = getClass().getResourceAsStream(
				"/groovy/BaseDublinCore.groovy");
		MappingScript<DublinCoreFunctionContext> script = factory.create(is1);
		DublinCoreRecord record = new DublinCoreRecordImpl();
		record.addTitle("test");
		DublinCoreFunctionContext dcContext = new DublinCoreFunctionContext(record);
		Map<String, Object> entries = script.parse(dcContext);
		Assert.assertEquals(entries.size(), 1);
		Assert.assertEquals(entries.get("title"), "test");
	}

}
