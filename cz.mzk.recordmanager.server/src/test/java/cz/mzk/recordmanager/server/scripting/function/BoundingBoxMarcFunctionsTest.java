package cz.mzk.recordmanager.server.scripting.function;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordFactory;
import cz.mzk.recordmanager.server.scripting.marc.function.BoundingBoxMarcFunctions;

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

	@Test
	public void testSantiago() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("034 $aa $b5100000 $dW0720810 $eW0475023 $fS0135757 $gS0345432");
		MarcRecord record = MarcRecordFactory.recordFactory(data);
		String result = functions.getBoundingBox(record );
		Assert.assertEquals(result, "-72.13611111111112 -34.90888888888889 -47.83972222222222 -13.965833333333332");
	}

	@Test
	public void testCzechRepublic() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("034 $aa $b200000 $dE0115843 $eE0185426 $fN0510158 $gN0483256");
		MarcRecord record = MarcRecordFactory.recordFactory(data);
		String result = functions.getBoundingBoxAsPolygon(record);
		Assert.assertEquals(result, "POLYGON((11.979 48.549, 18.907 48.549, 18.907 51.033, 11.979 51.033, 11.979 48.549))");
	}

	@Test
	public void testInvalidRecord() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("034 $aa $dE0142526 $eE0142526 $fN0500516 $gN0500516");
		MarcRecord record = MarcRecordFactory.recordFactory(data);
		String result = functions.getBoundingBox(record );
		Assert.assertNull(result);
	}

}
