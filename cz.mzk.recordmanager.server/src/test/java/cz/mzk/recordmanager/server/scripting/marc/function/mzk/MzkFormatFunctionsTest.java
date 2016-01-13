package cz.mzk.recordmanager.server.scripting.marc.function.mzk;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecordFactory;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;

@Test
public class MzkFormatFunctionsTest extends AbstractTest {

	@Autowired
	private MzkFormatFunctions formatFunctions;

	public void photographyFormat() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("072 $a77 $xfotografie");
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(format, "Photography");
	}

	public void photographyFormat2() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("072 $afotografie $xfd132277");
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(format, "Photography");
	}

	public void eletronicFormat() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("245 $honline [electronic resource]");
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(format, "Electronic");
	}

	public void normFormat() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("991 $n norma a patenty");
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(format, "Norm");
	}

	public void lawsOrOthersFormat() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("000 -----nai-a22-----2a-4500");
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(format, "LawsOrOthers");
	}

	public void physicalObjectFormat() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("000 -----nrm-a22-----2a-4500");
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(MarcRecordFactory.recordFactory(data)));
		Assert.assertEquals(format, "PhysicalObject");
	}

	public void unknownFormat() throws Exception {
		List<String> data = new ArrayList<String>();
		data.add("000 -----------------2a-4500");
		MarcRecordImpl record = MarcRecordFactory.recordFactory(data);
		String format = formatFunctions.getMZKFormat(new MarcFunctionContext(record));
		Assert.assertEquals(format, "Unknown");
	}

}
