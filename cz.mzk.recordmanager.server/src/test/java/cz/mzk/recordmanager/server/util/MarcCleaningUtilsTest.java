package cz.mzk.recordmanager.server.util;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MarcCleaningUtilsTest {

	@Test
	public void cleanFacets() {
		Assert.assertEquals(MarcCleaningUtils.cleanFacets(Arrays.asList("<foo")), Arrays.asList("<foo"));
	}

}
