package cz.mzk.recordmanager.server.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MetadataUtilsTest {

	@Test
	public void testNormalize() {
		Assert.assertEquals(MetadataUtils.normalize("Nechť již hříšné saxofony ďáblů "
				+ "rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu."),
				"nechtjizhrisnesaxofonydablurozzvucisinudesnymitonywaltzutangaaquickstepu");
		Assert.assertEquals(MetadataUtils.normalize("Verbüß öd’ Joch, kämpf Qual, zwing Styx!"),
				"verbuodjochkampfqualzwingstyx");
	}
	
	@Test
	public void testHasTrailingPunctuation() {
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa:"), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa;"), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa,"), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa="), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa("), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa["), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa ."), true);
		
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa"), false);
	}
}
