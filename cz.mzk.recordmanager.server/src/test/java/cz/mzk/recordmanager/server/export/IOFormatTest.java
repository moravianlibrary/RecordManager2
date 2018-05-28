package cz.mzk.recordmanager.server.export;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IOFormatTest {

	@Test
	public void stringToExportFormatTest() {
		Assert.assertEquals(IOFormat.stringToExportFormat("line"), IOFormat.LINE_MARC);
	}

	@Test
	public void getStringifyFormats() {
		Assert.assertEquals(IOFormat.getStringifyFormats().size(), IOFormat.values().length);
	}
}
