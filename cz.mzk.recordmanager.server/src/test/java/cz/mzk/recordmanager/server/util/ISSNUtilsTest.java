package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.util.identifier.ISSNUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ISSNUtilsTest {

	private static final String VALID_ISSN_EXAMPLE = "0317-8471";

	private static final String INVALID_ISSN_EXAMPLE = "1317-8471";

	@Test
	public void validISSN() {
		Assert.assertTrue(ISSNUtils.isValid(VALID_ISSN_EXAMPLE));
	}

	@Test
	public void invalidISSN() {
		Assert.assertFalse(ISSNUtils.isValid(INVALID_ISSN_EXAMPLE));
	}

}
