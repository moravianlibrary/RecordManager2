package cz.mzk.recordmanager.server.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class MarcCleaningUtilsTest {

	@Test
	public void cleanFacets() {
		Assert.assertEquals(MarcCleaningUtils.cleanFacets(Collections.singletonList("<foo")), Collections.singletonList("<foo"));
	}

}
