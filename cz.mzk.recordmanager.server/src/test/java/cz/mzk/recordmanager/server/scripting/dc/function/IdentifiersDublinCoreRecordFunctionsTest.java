package cz.mzk.recordmanager.server.scripting.dc.function;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.dc.DublinCoreRecordImpl;
import cz.mzk.recordmanager.server.scripting.dc.DublinCoreFunctionContext;

public class IdentifiersDublinCoreRecordFunctionsTest extends AbstractTest {

	@Autowired
	private IdentifiersDublinCoreRecordFunctions functions;

	@Test
	public void getISBNs() {
		DublinCoreRecord record = new DublinCoreRecordImpl();
		record.addIdentifier("ISBN:0385424728");
		DublinCoreFunctionContext dcContext = new DublinCoreFunctionContext(record);
		List<String> isbns = functions.getISBNs(dcContext);
		Assert.assertFalse(isbns.isEmpty());
		Assert.assertEquals(isbns.get(0), "0385424728");
	}

}
