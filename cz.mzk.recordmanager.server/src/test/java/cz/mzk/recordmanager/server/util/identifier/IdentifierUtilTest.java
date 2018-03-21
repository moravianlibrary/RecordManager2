package cz.mzk.recordmanager.server.util.identifier;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IdentifierUtilTest {

	@Test
	public void parseNoteTest() {
		Assert.assertEquals(IdentifierUtils.parseNote("(váz)"), "váz");
		Assert.assertEquals(IdentifierUtils.parseNote("váz"), "váz");
	}

}
