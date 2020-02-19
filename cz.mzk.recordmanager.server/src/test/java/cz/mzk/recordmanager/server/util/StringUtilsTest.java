package cz.mzk.recordmanager.server.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StringUtilsTest {

	@Test
	public void simmilarTitleMatchPercentageTest1() {
		String title1 = "klavirniskladbyclaviercompositionen";
		String title2 = "klavirniskladbytestclaviercompositionen";
		int per = StringUtils.simmilarTitleMatchPercentage(title1, title2, 70, 8);
		Assert.assertEquals(per, 88);
	}

	@Test
	public void simmilarTitleMatchPercentageTest2() {
		String title1 = "klavirniskladbyclaviercompositionen";
		String title2 = "szimfoniasinfonie";
		int per = StringUtils.simmilarTitleMatchPercentage(title1, title2, 70, 8);
		Assert.assertEquals(per, 0);
	}

}
