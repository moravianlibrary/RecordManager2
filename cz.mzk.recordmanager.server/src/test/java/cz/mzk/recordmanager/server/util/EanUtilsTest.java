package cz.mzk.recordmanager.server.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EanUtilsTest {

	private static final String VALID_EAN = "8593026341407";
	
	private static final String INVALID_EAN = "1234567891232";
	
	@Test
	public void isValid() {
		Assert.assertTrue(EANUtils.isEAN13valid(VALID_EAN));
	}
	
	@Test
	public void isInvalid() {
		Assert.assertFalse(EANUtils.isEAN13valid(INVALID_EAN));
	}
}
