package cz.mzk.recordmanager.server.scripting.function;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordFactory;

public class BoundingBoxMarcFunctionsTest extends AbstractTest {

	@Autowired
	private BoundingBoxMarcFunctions functions;

	@Test
	public void testPrague() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("034 $aa $b20000 $dE0141016 $eE0144724 $fN0501302 $gN0495545");
		MarcRecord record = MarcRecordFactory.recordFactory(data);
		String result = functions.getBoundingBox(record );
		Assert.assertEquals(result, "14.171111111111111 49.92916666666667 14.79 50.217222222222226");
	}
	
	@Test 
	public void testAustralia() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("034 $aa $b6000000 $dE1023922  $eE1784848 $fN0023252 $gS0474141");
		MarcRecord record = MarcRecordFactory.recordFactory(data);
		String result = functions.getBoundingBox(record );
		Assert.assertEquals(result, "102.65611111111112 -47.69472222222222 178.81333333333333 2.5477777777777777");
	}

}
